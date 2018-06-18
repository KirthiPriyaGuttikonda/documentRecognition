/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package documentrecognition;

import libsvm.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author gkirt
 */
public class Svm_train {
	private svm_parameter param;		// set by parse_command_line
	private svm_problem prob;		// set by read_problem
	private svm_model model;
	private String input_file_name;		// set by parse_command_line
	private String model_file_name;		// set by parse_command_line
	private String error_msg;
	private int cross_validation;
	private int nr_fold;

        static svm_model svmTrain(double[][] xtrain, double[] ytrain) {
        svm_problem prob = new svm_problem();
        int recordCount = xtrain.length;
        int featureCount = xtrain[0].length;
        prob.y = new double[recordCount];
        prob.l = recordCount;
        prob.x = new svm_node[recordCount][featureCount];     

        for (int i = 0; i < recordCount; i++){            
            double[] features = xtrain[i];
            prob.x[i] = new svm_node[features.length];
            for (int j = 0; j < features.length; j++){
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j] = node;
            }           
            prob.y[i] = ytrain[i];
        }               

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 100;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;       
        param.cache_size = 20000;
        param.eps = 0.001;      

        svm_model model = svm.svm_train(prob, param);

        return model;
    }  

  static double[] svmPredict(double[][] xtest, svm_model model) 
  {

      double[] yPred = new double[xtest.length];

      for(int k = 0; k < xtest.length; k++){

        double[] fVector = xtest[k];

        svm_node[] nodes = new svm_node[fVector.length];
        for (int i = 0; i < fVector.length; i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = fVector[i];
            nodes[i] = node;
        }

        int totalClasses = 5;       
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model,labels);

        double[] prob_estimates = new double[totalClasses];
        yPred[k] = svm.svm_predict_probability(model, nodes, prob_estimates);

      }

      return yPred;
  } 

        
	private static svm_print_interface svm_print_null = new svm_print_interface()
	{
		public void print(String s) {}
	};

	private static void exit_with_help()
	{
		System.out.print(
		 "Usage: svm_train [options] training_set_file [model_file]\n"
		+"options:\n"
		+"-s svm_type : set type of SVM (default 0)\n"
		+"	0 -- C-SVC		(multi-class classification)\n"
		+"	1 -- nu-SVC		(multi-class classification)\n"
		+"	2 -- one-class SVM\n"
		+"	3 -- epsilon-SVR	(regression)\n"
		+"	4 -- nu-SVR		(regression)\n"
		+"-t kernel_type : set type of kernel function (default 2)\n"
		+"	0 -- linear: u'*v\n"
		+"	1 -- polynomial: (gamma*u'*v + coef0)^degree\n"
		+"	2 -- radial basis function: exp(-gamma*|u-v|^2)\n"
		+"	3 -- sigmoid: tanh(gamma*u'*v + coef0)\n"
		+"	4 -- precomputed kernel (kernel values in training_set_file)\n"
		+"-d degree : set degree in kernel function (default 3)\n"
		+"-g gamma : set gamma in kernel function (default 1/num_features)\n"
		+"-r coef0 : set coef0 in kernel function (default 0)\n"
		+"-c cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)\n"
		+"-n nu : set the parameter nu of nu-SVC, one-class SVM, and nu-SVR (default 0.5)\n"
		+"-p epsilon : set the epsilon in loss function of epsilon-SVR (default 0.1)\n"
		+"-m cachesize : set cache memory size in MB (default 100)\n"
		+"-e epsilon : set tolerance of termination criterion (default 0.001)\n"
		+"-h shrinking : whether to use the shrinking heuristics, 0 or 1 (default 1)\n"
		+"-b probability_estimates : whether to train a SVC or SVR model for probability estimates, 0 or 1 (default 0)\n"
		+"-wi weight : set the parameter C of class i to weight*C, for C-SVC (default 1)\n"
		+"-v n : n-fold cross validation mode\n"
		+"-q : quiet mode (no outputs)\n"
		);
		System.exit(1);
	}

	private void do_cross_validation()
	{
		int i;
		int total_correct = 0;
		double total_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double[] target = new double[prob.l];

		svm.svm_cross_validation(prob,param,nr_fold,target);
		if(param.svm_type == svm_parameter.EPSILON_SVR ||
		   param.svm_type == svm_parameter.NU_SVR)
		{
			for(i=0;i<prob.l;i++)
			{
				double y = prob.y[i];
				double v = target[i];
				total_error += (v-y)*(v-y);
				sumv += v;
				sumy += y;
				sumvv += v*v;
				sumyy += y*y;
				sumvy += v*y;
			}
			System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
			System.out.print("Cross Validation Squared correlation coefficient = "+
				((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
				((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
				);
		}
		else
		{
			for(i=0;i<prob.l;i++)
				if(target[i] == prob.y[i])
					++total_correct;
			System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
		}
	}
	
	private void run(String argv[]) throws IOException
	{
		parse_command_line(argv);
		read_problem();
		error_msg = svm.svm_check_parameter(prob,param);

		if(error_msg != null)
		{
			System.err.print("ERROR: "+error_msg+"\n");
			System.exit(1);
		}

		if(cross_validation != 0)
		{
			do_cross_validation();
		}
		else
		{
			model = svm.svm_train(prob,param);
			svm.svm_save_model(model_file_name,model);
		}
	}

	public static void main(String argv[]) throws IOException
	{

            boolean retrain = false;
            svm_model m;
            //if(!retrain)
                m = svm.svm_load_model("tam_model");
                double[][] xtrain = new double[2000][4157];
                double[] ytrain = new double[2000];
                double[][] xtest = new double[1][4157];
                double[] ytest = new double[1];
                //Read the features file to xtrain, and assign the class label ytrain
                System.out.println("Training data");
                int count = 0;
                int []number_of_files_in_each_category = {510 - 45,386-45,417-45,511-45,401-45};
                for(int i = 1;i<=5;i++){
                    for (int j = 1;j<=number_of_files_in_each_category[i-1];j++)
                    {
                        System.out.println(count);
                        StringBuilder sbuf = new StringBuilder();
                        Formatter fmt = new Formatter(sbuf);
                        fmt.format("%03d", j);
                        String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        File file = new File(filename);
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                                xtrain[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                        }
                        fileReader.close();
                        ytrain[count] = i;
                        count++;

                }


                }

                System.out.println("Testing data");
                count = 0;
                //for(int i = 1;i<=5;i++)
                //{
                  //  for (int j = number_of_files_in_each_category[i-1]+1;j<=number_of_files_in_each_category[i-1]+45;j++)
                  //  {
                        System.out.println(count);

                        //StringBuilder sbuf = new StringBuilder();
                        //Formatter fmt = new Formatter(sbuf);
                        //fmt.format("%03d", j);
                        
                        //String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        
                        String filename = "C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\trial\\sport.txt.bow";
                       
                        System.out.println(filename);
                        File file = new File(filename);
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                               xtest[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                            if(line_id == 4158)
                                break;
                        }
                        fileReader.close();
                        //ytest[count] = i;
                        count++;
                    //}

                //}

                //Normalize the features for training data
                for(int i = 0; i<2000; i++) 
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtrain[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {
                        xtrain[i][j] /= sum;
                    }
                }

                //Normalize the features for testing data
                for(int i = 0; i<1; i++)
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtest[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {                        
                        xtest[i][j] /= sum;
                    }
                }
                
            if(retrain){
                m = svmTrain(xtrain,ytrain);
                svm.svm_save_model("tam_model",m);
            }
            double[] ypred = svmPredict(xtest, m); 
            String [] labels = {"business","entertainment","politics","sports","tech"};
      for (int i = 0; i < xtest.length; i++){
          //if(ytest[i]!=ypred[i])
            //System.out.println(i + ": (Actual:" + ytest[i] + " Prediction:" + ypred[i] + ")"); 
      System.out.println(" Prediction:" + ypred[i] + " "  + labels[(int)(ypred[i])-1] );
      }  

      
                //Svm_train t = new Svm_train();
		//t.run(argv);
	}
        
        //Tam's code
        public static void main2(String argv[]) throws IOException
	{
//		double[][] xtrain = {{1,1,1,1},{2,2,2,2},{3,3,3,3},{4,4,4,4}};
//                double[][] xtest = {{1.5,1.5,1.5,1.5},{5,5,5,5}};
//                double[] ytrain = {1,1,2,2,};
//                double[] ytest = {1,2};
//
//          
            boolean retrain = false;
            svm_model m;
            //if(!retrain)
                m = svm.svm_load_model("tam_model");
                double[][] xtrain = new double[2000][4157];
                double[] ytrain = new double[2000];
                double[][] xtest = new double[225][4157];
                double[] ytest = new double[225];
                //Read the features file to xtrain, and assign the class label ytrain
                System.out.println("Training data");
                int count = 0;
                int []number_of_files_in_each_category = {510 - 45,386-45,417-45,511-45,401-45};
                for(int i = 1;i<=5;i++){
                    for (int j = 1;j<=number_of_files_in_each_category[i-1];j++)
                    {
                        System.out.println(count);
                        StringBuilder sbuf = new StringBuilder();
                        Formatter fmt = new Formatter(sbuf);
                        fmt.format("%03d", j);
                        String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        File file = new File(filename);
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                                xtrain[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                        }
                        fileReader.close();
                        ytrain[count] = i;
                        count++;

                }


                }

                System.out.println("Testing data");
                count = 0;
                for(int i = 1;i<=5;i++){
                    for (int j = number_of_files_in_each_category[i-1]+1;j<=number_of_files_in_each_category[i-1]+45;j++)
                    {
                        System.out.println(count);

                        StringBuilder sbuf = new StringBuilder();
                        Formatter fmt = new Formatter(sbuf);
                        fmt.format("%03d", j);
                        
                        String filename = "c:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\bbc\\tam_all_classes\\" + i + "_" + sbuf.toString()+".txt.bow";
                        
                        //String filename = "C:\\Users\\mudig\\Desktop\\Tam Project\\2nd semester\\Code\\FileTree\\trial\\" + i + "_" + sbuf.toString()+".txt.bow";
                       
                        System.out.println(filename);
                        File file = new File(filename);
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        int line_id = 0;
                        while ((line = bufferedReader.readLine()) != null) {
                            if(line_id > 0)
                               xtest[count][line_id-1] = Integer.parseInt(line);
                            line_id++;
                            if(line_id == 4158)
                                break;
                        }
                        fileReader.close();
                        ytest[count] = i;
                        count++;
                    }

                }

                //Normalize the features for training data
                for(int i = 0; i<2000; i++) 
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtrain[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {
                        xtrain[i][j] /= sum;
                    }
                }

                //Normalize the features for testing data
                for(int i = 0; i<225; i++)
                {
                    long sum = 0;
                    for(int j = 0; j<4157; j++)
                    {
                        sum += xtest[i][j];

                    }
                    for(int j = 0; j<4157; j++)
                    {                        
                        xtest[i][j] /= sum;
                    }
                }
                
            if(retrain){
                m = svmTrain(xtrain,ytrain);
                svm.svm_save_model("tam_model",m);
            }
            double[] ypred = svmPredict(xtest, m); 

      for (int i = 0; i < xtest.length; i++){
          if(ytest[i]!=ypred[i])
            System.out.println(i + ": (Actual:" + ytest[i] + " Prediction:" + ypred[i] + ")"); 
      }  

      
                //Svm_train t = new Svm_train();
		//t.run(argv);
	}

	private static double atof(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

	private void parse_command_line(String argv[])
	{
		int i;
		svm_print_interface print_func = null;	// default printing to stdout

		param = new svm_parameter();
		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		cross_validation = 0;

		// parse options
		for(i=0;i<argv.length;i++)
		{
			if(argv[i].charAt(0) != '-') break;
			if(++i>=argv.length)
				exit_with_help();
			switch(argv[i-1].charAt(1))
			{
				case 's':
					param.svm_type = atoi(argv[i]);
					break;
				case 't':
					param.kernel_type = atoi(argv[i]);
					break;
				case 'd':
					param.degree = atoi(argv[i]);
					break;
				case 'g':
					param.gamma = atof(argv[i]);
					break;
				case 'r':
					param.coef0 = atof(argv[i]);
					break;
				case 'n':
					param.nu = atof(argv[i]);
					break;
				case 'm':
					param.cache_size = atof(argv[i]);
					break;
				case 'c':
					param.C = atof(argv[i]);
					break;
				case 'e':
					param.eps = atof(argv[i]);
					break;
				case 'p':
					param.p = atof(argv[i]);
					break;
				case 'h':
					param.shrinking = atoi(argv[i]);
					break;
				case 'b':
					param.probability = atoi(argv[i]);
					break;
				case 'q':
					print_func = svm_print_null;
					i--;
					break;
				case 'v':
					cross_validation = 1;
					nr_fold = atoi(argv[i]);
					if(nr_fold < 2)
					{
						System.err.print("n-fold cross validation: n must >= 2\n");
						exit_with_help();
					}
					break;
				case 'w':
					++param.nr_weight;
					{
						int[] old = param.weight_label;
						param.weight_label = new int[param.nr_weight];
						System.arraycopy(old,0,param.weight_label,0,param.nr_weight-1);
					}

					{
						double[] old = param.weight;
						param.weight = new double[param.nr_weight];
						System.arraycopy(old,0,param.weight,0,param.nr_weight-1);
					}

					param.weight_label[param.nr_weight-1] = atoi(argv[i-1].substring(2));
					param.weight[param.nr_weight-1] = atof(argv[i]);
					break;
				default:
					System.err.print("Unknown option: " + argv[i-1] + "\n");
					exit_with_help();
			}
		}

		svm.svm_set_print_string_function(print_func);

		// determine filenames

		if(i>=argv.length)
			exit_with_help();

		input_file_name = argv[i];

		if(i<argv.length-1)
			model_file_name = argv[i+1];
		else
		{
			int p = argv[i].lastIndexOf('/');
			++p;	// whew...
			model_file_name = argv[i].substring(p)+".model";
		}
	}

	// read in a problem (in svmlight format)

	private void read_problem() throws IOException
	{
		BufferedReader fp = new BufferedReader(new FileReader(input_file_name));
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(atof(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0)
			param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}

		fp.close();
	}
}
    
    

