package model;

import util.MyArray;
import corpus.Instance;

public class ForwardBackwardScaled extends ForwardBackward{
	double[] scale;
	public ForwardBackwardScaled(HMM model, Instance instance) {
		super();
		this.model = model;
		this.instance = instance;
		this.nrStates = model.nrStates;
		T = instance.T; 
		initial = model.param.initial;
		transition = model.param.transition;
		observation = model.param.observation;		
		scale = new double[T+1];		
	}
	
	@Override
	public void doInference() {
		forward();
		backward();
		computePosterior();
	}
	
	@Override
	public void forward() {
		alpha = new double[T+1][nrStates]; //+1 for fake state
		logLikelihood = 0;
		//for t=0
		for(int i=0; i<nrStates; i++) {
			double pi = initial.get(i, 0);
			double obs = observation.get( instance.words[0], i);
			alpha[0][i] = pi * obs;
			if(alpha[0][i] == 0) {
				System.out.println("ZERO alpha at initial");
				System.exit(-1);
			}
			scale[0] += alpha[0][i];
		}
		for(int i=0; i<nrStates; i++) {
			alpha[0][i] = alpha[0][i] / scale[0];
		}
		logLikelihood += Math.log(scale[0]);
		if(Double.isNaN(Math.log(scale[0]))) {
			System.out.println("Scale = " + scale[0]);
			MyArray.printTable(alpha);
			throw new RuntimeException("logLikelihood at initial position in forward is NaN");
		}
		for(int t = 1; t < T+1; t++) {
			for(int j=0; j<nrStates; j++) {
				double transSum = 0;
				for(int i=0; i<nrStates; i++) {
					transSum += alpha[t-1][i] * transition.get(j, i);
				}
				double obs;
				if(t == T) {
					//fake state
					obs = 1.0;
				} else {
					obs = observation.get(instance.words[t], j);
					//fix
					if(obs == 0) {
						obs = 1e-100;
					}
				}
				alpha[t][j] = transSum * obs;
				scale[t] += alpha[t][j];
			}
			//scale
			for(int j=0; j<nrStates; j++) {
				if(scale[t] <= 0) {
					System.err.println("Scale is not positive");
					System.exit(-1);
				}
				alpha[t][j] = alpha[t][j] / scale[t];				
			}
			logLikelihood += Math.log(scale[t]);
			if(Double.isNaN(logLikelihood)) {
				System.out.println(scale[t]);
				MyArray.printTable(alpha);
				throw new RuntimeException("logLikelihood is NaN");
			}
		}
		//MyArray.printTable(alpha);
		System.out.println("LogLikelihood: " + logLikelihood);
	}
	
	@Override
	public void backward() {
		beta = new double[T+1][nrStates];
		//initialization for t=T
		for(int i=0; i<nrStates; i++) {
			beta[T][i] = scale[T] * 1.0;
		}
		//induction
		for(int t=T-1; t>=0; t--) {
			for(int i=0; i<nrStates; i++) {			
				double sum = 0;
				for(int j=0; j<nrStates; j++) {
					double trans = transition.get(j, i);
					double obs;
					if(t == T-1) {
						obs = 1.0; //taken for the fake state(at t+1)
					} else {
						obs = observation.get(instance.words[t+1], j);
					}
					sum += trans * obs * beta[t+1][j];
				}
				beta[t][i] = scale[t] * sum;
			}
		}
		//MyArray.printTable(beta);
	}
	
	@Override
	public void computePosterior() {
		posterior = new double[T][nrStates];
		for(int t=0; t<T; t++) {
			double denom = 0;
			
			for(int i=0; i<nrStates; i++) {
				denom += alpha[t][i] * beta[t][i];
			}
			
			for(int i=0; i<nrStates; i++) {
				posterior[t][i] = alpha[t][i] * beta[t][i] / denom;
			}
		}
		//MyArray.printTable(posterior);
		checkStatePosterior();
	}
	
	public void checkStatePosterior(){
		double tolerance = 1e-5;
		for(int t=0; t<T; t++) {
			double sum = 0;
			for(int i=0; i<nrStates; i++) {
				if(Double.isInfinite(posterior[t][i])){
					throw new RuntimeException("State posterior infinite while checking");
				}
				if(Double.isNaN(posterior[t][i])){
					throw new RuntimeException("State posterior NaN while checking");
				}
				sum += posterior[t][i];
			}
			if(Math.abs(sum - 1) > tolerance) {
				throw new RuntimeException("In checking state posterior, sum = " + sum);
			}
		}
	}
	
	@Override
	public void checkForwardBackward() {
				
	}
}