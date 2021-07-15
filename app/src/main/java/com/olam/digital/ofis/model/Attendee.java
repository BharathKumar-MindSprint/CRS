package com.olam.digital.ofis.model;

public class Attendee {

    private int farmerId;
    private  int  trainingId;
    private String traineeName;
    private String traineeAge;
    private String traineeGender;
    private String traineeGenderId;
    private String traineeRelationship;
    private String traineeRelationshipId;

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }

    public int getTrainingId() {
        return trainingId;
    }

    public void setTrainingId(int trainingId) {
        this.trainingId = trainingId;
    }

    public String getTraineeName() {
        return traineeName;
    }

    public void setTraineeName(String traineeName) {
        this.traineeName = traineeName;
    }

    public String getTraineeAge() {
        return traineeAge;
    }

    public void setTraineeAge(String traineeAge) {
        this.traineeAge = traineeAge;
    }

    public String getTraineeGender() {
        return traineeGender;
    }

    public void setTraineeGender(String traineeGender) {
        this.traineeGender = traineeGender;
    }

    public String getTraineeGenderId() {
        return traineeGenderId;
    }

    public void setTraineeGenderId(String traineeGenderId) {
        this.traineeGenderId = traineeGenderId;
    }

    public String getTraineeRelationship() {
        return traineeRelationship;
    }

    public void setTraineeRelationship(String traineeRelationship) {
        this.traineeRelationship = traineeRelationship;
    }

    public String getTraineeRelationshipId() {
        return traineeRelationshipId;
    }

    public void setTraineeRelationshipId(String traineeRelationshipId) {
        this.traineeRelationshipId = traineeRelationshipId;
    }
}
