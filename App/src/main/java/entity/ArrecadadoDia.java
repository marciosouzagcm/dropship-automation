package entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ArrecadadoDia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Identificador único para cada registro
    private String data; // Data do arrecadado
    private float arrecadadoUber; // Valor arrecadado pelo Uber
    private float arrecadado99; // Valor arrecadado pelo 99
    private float arrecadadoIndrive; // Valor arrecadado pelo InDrive
    private float arrecadadoOutros; // Valor arrecadado por outros meios

    // Getter e Setter para o atributo data
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Getter e Setter para o atributo arrecadadoUber
    public float getArrecadadoUber() {
        return arrecadadoUber;
    }

    public void setArrecadadoUber(float arrecadadoUber) {
        this.arrecadadoUber = arrecadadoUber;
    }

    // Getter e Setter para o atributo arrecadado99
    public float getArrecadado99() {
        return arrecadado99;
    }

    public void setArrecadado99(float arrecadado99) {
        this.arrecadado99 = arrecadado99;
    }

    // Getter e Setter para o atributo arrecadadoIndrive
    public float getArrecadadoIndrive() {
        return arrecadadoIndrive;
    }

    public void setArrecadadoIndrive(float arrecadadoIndrive) {
        this.arrecadadoIndrive = arrecadadoIndrive;
    }

    // Getter e Setter para o atributo arrecadadoOutros
    public float getArrecadadoOutros() {
        return arrecadadoOutros;
    }

    public void setArrecadadoOutros(float arrecadadoOutros) {
        this.arrecadadoOutros = arrecadadoOutros;
    }

    public static void main(String[] args) {
        // Método principal, pode ser usado para testes
    }
}