package entity;

import java.time.LocalDate; // Importação da classe LocalDate para representar a data

public class DesempenhoDia {
    
    // Atributos da classe Desempenho
    private LocalDate data; // Data do desempenho
    private int corridasDiaUber; // Número de corridas no Uber
    private int corridasDia99; // Número de corridas no 99
    private int corridasDiaIndrive; // Número de corridas no InDrive
    private String onlineUber; // Tempo online no Uber (formato HH:mm)
    private String online99; // Tempo online no 99 (formato HH:mm)
    private String onlineIndrive; // Tempo online no InDrive (formato HH:mm)
    private float totalKmDia; // Total de quilômetros percorridos no dia

    // Getter e Setter para o atributo data
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    // Getter e Setter para o atributo corridasDiaUber
    public int getCorridasDiaUber() {
        return corridasDiaUber;
    }

    public void setCorridasDiaUber(int corridasDiaUber) {
        this.corridasDiaUber = corridasDiaUber;
    }

    // Getter e Setter para o atributo corridasDia99
    public int getCorridasDia99() {
        return corridasDia99;
    }

    public void setCorridasDia99(int corridasDia99) {
        this.corridasDia99 = corridasDia99;
    }

    // Getter e Setter para o atributo corridasDiaIndrive
    public int getCorridasDiaIndrive() {
        return corridasDiaIndrive;
    }

    public void setCorridasDiaIndrive(int corridasDiaIndrive) {
        this.corridasDiaIndrive = corridasDiaIndrive;
    }

    // Getter e Setter para o atributo onlineUber
    public String getOnlineUber() {
        return onlineUber;
    }

    public void setOnlineUber(String onlineUber) {
        this.onlineUber = onlineUber;
    }

    // Getter e Setter para o atributo online99
    public String getOnline99() {
        return online99;
    }

    public void setOnline99(String online99) {
        this.online99 = online99;
    }

    // Getter e Setter para o atributo onlineIndrive
    public String getOnlineIndrive() {
        return onlineIndrive;
    }

    public void setOnlineIndrive(String onlineIndrive) {
        this.onlineIndrive = onlineIndrive;
    }

    // Getter e Setter para o atributo totalKmDia
    public float getTotalKmDia() {
        return totalKmDia;
    }

    public void setTotalKmDia(float totalKmDia) {
        this.totalKmDia = totalKmDia;
    }
}