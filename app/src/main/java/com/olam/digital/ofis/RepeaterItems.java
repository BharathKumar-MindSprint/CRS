package com.olam.digital.ofis;

public class RepeaterItems {
    public int questionId;
    public int repeaterPosition;

    public RepeaterItems(int questionId, int repeaterPosition) {
        this.questionId = questionId;
        this.repeaterPosition = repeaterPosition;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RepeaterItems other = (RepeaterItems) obj;

        return questionId == other.questionId && repeaterPosition == other.repeaterPosition;
    }


}
