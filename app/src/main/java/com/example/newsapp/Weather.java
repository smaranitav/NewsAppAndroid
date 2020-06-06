package com.example.newsapp;

public class Weather {

    public Location location;
    public CurrentCondition currentCondition = new CurrentCondition();
    public Temperature temperature = new Temperature();

    public byte[] iconData;

    public  class CurrentCondition {
        private int weatherId;
        private String condition;
        private String descr;
        private String icon;

        public int getWeatherId() {
            return weatherId;
        }
        public void setWeatherId(int weatherId) {
            this.weatherId = weatherId;
        }
        public String getCondition() {
            return condition;
        }
        public void setCondition(String condition) {
            this.condition = condition;
        }
        public String getDescr() {
            return descr;
        }

        public String getIcon() {
            return icon;
        }
        public void setIcon(String icon) {
            this.icon = icon;
        }

    }

    public  class Temperature {
        private float temp;

        public float getTemp() {
            return temp;
        }
        public void setTemp(float temp) {
            this.temp = temp;
        }
    }

}
