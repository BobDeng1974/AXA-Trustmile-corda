

package com.sidis.eas.client.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "policyNumber",
        "vin",
        "car",
        "insurer",
        "mileagePerYear",
        "mileageState",
        "accidentState",
        "insuranceRate",
        "data"
})
public class CarPolicy {

    @JsonProperty("policyNumber")
    private String policyNumber;
    @JsonProperty("vin")
    private String vin;
    @JsonProperty("car")
    private String car;
    @JsonProperty("insurer")
    private String insurer;
    @JsonProperty("mileagePerYear")
    private Integer mileagePerYear;
    @JsonProperty("mileageState")
    private String mileageState;
    @JsonProperty("accidentState")
    private String accidentState;
    @JsonProperty("insuranceRate")
    private Integer insuranceRate;
    @JsonProperty("data")
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    @JsonProperty("policyNumber")
    public String getPolicyNumber() {
        return policyNumber;
    }

    @JsonProperty("policyNumber")
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    @JsonProperty("vin")
    public String getVin() {
        return vin;
    }

    @JsonProperty("vin")
    public void setVin(String vin) {
        this.vin = vin;
    }

    @JsonProperty("car")
    public String getCar() {
        return car;
    }

    @JsonProperty("car")
    public void setCar(String car) {
        this.car = car;
    }

    @JsonProperty("insurer")
    public String getInsurer() {
        return insurer;
    }

    @JsonProperty("insurer")
    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    @JsonProperty("mileagePerYear")
    public Integer getMileagePerYear() {
        return mileagePerYear;
    }

    @JsonProperty("mileagePerYear")
    public void setMileagePerYear(Integer mileagePerYear) {
        this.mileagePerYear = mileagePerYear;
    }

    @JsonProperty("mileageState")
    public String getMileageState() {
        return mileageState;
    }

    @JsonProperty("mileageState")
    public void setMileageState(String mileageState) {
        this.mileageState = mileageState;
    }

    @JsonProperty("accidentState")
    public String getAccidentState() {
        return accidentState;
    }

    @JsonProperty("accidentState")
    public void setAccidentState(String accidentState) {
        this.accidentState = accidentState;
    }

    @JsonProperty("insuranceRate")
    public Integer getInsuranceRate() {
        return insuranceRate;
    }

    @JsonProperty("insuranceRate")
    public void setInsuranceRate(Integer insuranceRate) {
        this.insuranceRate = insuranceRate;
    }

    public void setData(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    @JsonIgnore
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }
    // Do we need these getters and setters
 /*   @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/

    public String toString() {
        return this.policyNumber + "," + this.vin + "," + this.insurer + ","
                + this.mileagePerYear + "," + this.accidentState + ","
                + this.car;
    }

}