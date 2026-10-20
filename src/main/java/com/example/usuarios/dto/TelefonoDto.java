package com.example.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public class TelefonoDto {

    @NotBlank(message = "El número de teléfono es obligatorio")
    private String number;

    @NotBlank(message = "El citycode es obligatorio")
    private String citycode;

    @NotBlank(message = "El contrycode es obligatorio")
    private String contrycode;

    public TelefonoDto() {
    }

    public TelefonoDto(String number, String citycode, String contrycode) {
        this.number = number;
        this.citycode = citycode;
        this.contrycode = contrycode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCitycode() {
        return citycode;
    }

    public void setCitycode(String citycode) {
        this.citycode = citycode;
    }

    public String getContrycode() {
        return contrycode;
    }

    public void setContrycode(String contrycode) {
        this.contrycode = contrycode;
    }
}
