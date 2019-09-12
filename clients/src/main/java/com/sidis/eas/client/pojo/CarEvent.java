package com.sidis.eas.client.pojo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "car",
        "vin",
        "timestamp",
        "mileage",
        "accident",
        "data"
})
public class CarEvent {

    @JsonProperty("car")
    private String car;
    @JsonProperty("vin")
    private String vin;
    @JsonProperty("timestamp")
    private Integer timestamp;
    @JsonProperty("mileage")
    private Integer mileage;
    @JsonProperty("accident")
    private Boolean accident;
    @JsonProperty("data")
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("car")
    public String getCar() {
        return car;
    }

    @JsonProperty("car")
    public void setCar(String car) {
        this.car = car;
    }

    @JsonProperty("vin")
    public String getVin() {
        return vin;
    }

    @JsonProperty("vin")
    public void setVin(String vin) {
        this.vin = vin;
    }

    @JsonProperty("timestamp")
    public Integer getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("mileage")
    public Integer getMileage() {
        return mileage;
    }

    @JsonProperty("mileage")
    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    @JsonProperty("accident")
    public Boolean getAccident() {
        return accident;
    }

    @JsonProperty("accident")
    public void setAccident(Boolean accident) {
        this.accident = accident;
    }

    public void setData(String key, Object value) {
        this.additionalProperties.put(key, value);
    }


    /*

    // Do we need this any getter and any setter? I am afraid they can mess up the interfaces
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    */

    public String toString(){
        return "Car: " +  this.car +", Vin" + this.vin + ", Accident" + this.accident;
    }

}
