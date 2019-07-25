package com.tima.top.friends.algs.arfc;


import com.tima.top.friends.algs.AbsRankingAlgorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class ArfcAlgorithm extends AbsRankingAlgorithm {
    private float[][] inpuMatrix;
    private Float[] score;
    private float[] idealVector;
    private float[] unidealFriendVector;
    private int[] attitudeFeatureVector;

    private int m;
    private int n;

    public ArfcAlgorithm(float[][] inpuMatrix, int[] attitudeFeatureVector) {
        this.inpuMatrix = inpuMatrix;
        this.attitudeFeatureVector = attitudeFeatureVector;
        if(inpuMatrix.length > 0){
            m = inpuMatrix.length;
        } else throw new IllegalArgumentException("invalid argument, matrix lenght must be greater than 0 !");
        if (inpuMatrix[0].length > 0){
            n = inpuMatrix[0].length;
        } else throw new IllegalArgumentException("invalid argument, matrix lenght must be greater than 0 !");

        if (attitudeFeatureVector.length != n)
            throw new IllegalArgumentException("invalid positive/negative feature vector, length must be " + n + " !");

        score = new Float[m];
    }

    @Override
    public void scoring() {
        float[][] normalMatrix = normalizeMatrix(inpuMatrix);
        idealVector = getIdealFriendVector(normalMatrix);
        unidealFriendVector = getUnIdealFriendVector(normalMatrix);
        getFriendScores(normalMatrix,idealVector,unidealFriendVector);
    }

    @Override
    public void ranking() {
        Arrays.sort(score, Collections.reverseOrder());
    }

    public float[][] normalizeMatrix(float[][] matrix){
        float[][] result = new float[m][n];
        int[] totalCol = new int[n];
        for(int j=0 ; j<n; j++)
        {
            for(int i =0;i<m;i++){
                totalCol[j] += Math.pow(matrix[i][j],2);
            }
        }
        for(int j=0 ; j<n; j++)
        {
            for(int i =0;i<m;i++){
                if(totalCol[j] != 0)
                    result[i][j] = matrix[i][j]*1.0F/totalCol[j];
                else
                    result[i][j] = 0;
            }
        }
        return result;
    }

    public float[][] convertToWeightMatrix(float[][] matrix,float[] weights){
        if(weights.length != n)
            throw new IllegalArgumentException("invalid weight vector, length must be " + n + " !");
        float[][] result = new float[m][n];
        for(int j=0;j<n;j++){
            for(int i=0;i<m;i++){
                result[i][j] = weights[j]*matrix[i][j];
            }
        }
        return result;
    }

    public float[] getIdealFriendVector(float[][] wMatrix) {
        // attitudeFeatureVector dang [0,1,1,0,0 ...] 1 - feature tich cuc, 0 - feature tieu cuc
        float[] idealFeature = new float[n];
        for (int j = 0; j < n; j++) {
            float max = wMatrix[0][j], min = wMatrix[0][j];
            if (attitudeFeatureVector[j] != 0 && attitudeFeatureVector[j] != 1)
                throw new IllegalArgumentException("invalid positive/negative feature vector, value not in (0,1) !!");
            // get max value at positive columns
            if (attitudeFeatureVector[j] == 1) {
                for (int i = 0; i < m; i++) {
                    if (wMatrix[i][j] > max)
                        max = wMatrix[i][j];
                }
                idealFeature[j] = max;
            }
            // get min value at negative columns
            if (attitudeFeatureVector[j] == 0) {
                for (int i = 0; i < m; i++) {
                    if (wMatrix[i][j] < min)
                        min = wMatrix[i][j];
                }
                idealFeature[j] = min;
            }
        }
        return idealFeature;
    }
    
    public float[] getUnIdealFriendVector(float[][] wMatrix){
        // attitudeFeatureVector dang [0,1,1,0,0 ...] 1 - feature tich cuc, 0 - feature tieu cuc
        float[] unIdealFeature = new float[n];
        for(int j=0;j<n;j++){
            float max = wMatrix[0][j],min = wMatrix[0][j];
            if(attitudeFeatureVector[j] != 0 && attitudeFeatureVector[j] != 1)
                throw new IllegalArgumentException("invalid positive/negative feature vector, value not in (0,1) !!");
            // get min value
            if(attitudeFeatureVector[j] == 1){
                for(int i=0;i< m;i++){
                    if(wMatrix[i][j] < min)
                        min = wMatrix[i][j];
                }
                unIdealFeature[j] = min;
            }
            // get max value
            if(attitudeFeatureVector[j] == 0){
                for(int i=0;i< m;i++){
                    if(wMatrix[i][j] > max)
                        max = wMatrix[i][j];
                }
                unIdealFeature[j] = max;
            }
        }
        return unIdealFeature;
    }

    // tinh score cho m friends
    public Float[] getFriendScores(float[][] wMatrix, float[] idealFeature, float[] unIdealFeature){
        for(int i=0;i<m;i++){
            float diit=0, dimu=0;
            for(int j=0;j<n;j++){
                diit += Math.pow(wMatrix[i][j]-idealFeature[j],2);
                dimu += Math.pow(wMatrix[i][j]-unIdealFeature[j],2);
            }
            diit = (float) Math.sqrt(diit);
            dimu = (float) Math.sqrt(dimu);
            score[i] = dimu/(diit+dimu);
        }
        return score;
    }

    public float[][] randomInputMatrix(int rowNum, int columnNum){
        float[][] result = new float[rowNum][columnNum];
        Random ran = new Random();
        for(int i=0;i<rowNum;i++){
            for(int j=0;j<4;j++){
                result[i][j] = ran.nextInt(1000);
            }
        }
        return result;
    }

    public Float[] getScore() {
        return score;
    }
}
