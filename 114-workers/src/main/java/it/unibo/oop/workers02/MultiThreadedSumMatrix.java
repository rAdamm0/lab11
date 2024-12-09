package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix{
    private final int nthread;

    public MultiThreadedSumMatrix(final int nthread){
        super();
        if(nthread<1){
            throw new IllegalArgumentException();
        }
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double [][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        Worker(final double [][] matrix, final int startpos, final int nelem){
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        public void run(){
            for (int i = startpos; i < matrix.length && i < startpos + nelem; i++){
                for (final double d : this.matrix[i]){
                    this.res += d;
                }
            }
        }
    
        public double getResult(){
            return this.res;
        }

    }








    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;

        final List<Worker> workers = new ArrayList<>(nthread);
        
        for (int start = 0; start < matrix.length; start += size){
            workers.add(new Worker(matrix, start, size));
        }

        for (final Thread w : workers){
            w.start();
        }

        double sum = 0;
        for (final Worker w : workers){
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        return sum;
        
    }

}
