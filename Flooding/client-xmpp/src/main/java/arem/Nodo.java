package arem;

import java.util.*;

public class Nodo {
    public int [] trayecto;
    public String [] trayectoNodos;
    public String nodo;
    public String names;
    public int idNodo;
    public boolean recibido;
    public int saltos = 0;
    public String message;


    //Nodo class constructor
    public void setData(int []id, String[] idName, String name, int idNodo, boolean recibio, String correo){
        this.trayecto = Arrays.copyOf(id, id.length);
        this.trayectoNodos = Arrays.copyOf(idName, idName.length);
        this.nodo = name;
        this.idNodo = idNodo;
        this.recibido = recibio;
        this.names = correo;
    }

    public String Message(String s, String d){
        return this.message = "----------------------Mensaje----------------------" + "\n" +
                              "Nodo fuente: " + s + "\n" +
                              "Nodo destino: " + d + "\n" +
                              "Saltos dados desde nodo fuente: " + saltos + "\n";
    }

    //Setter
    public void setRecibido(boolean recibio){
        this.recibido = recibio;
    }

    public void setSatlos(int cant){
        this.saltos = cant + 1;
    }

    //Getter
    public boolean getRecibido(){
        return recibido;
    }

    public int[] getTrayecto(){
        return trayecto;
    }

    public String getNodo(){
        return nodo;
    }

    public int getIdNodo(){
        return idNodo;
    }

    public String[] getTrayectoNodos(){
        return trayectoNodos;
    }

    public String getNames(){
        return names;
    }

    public int getSaltos(){
        return saltos;
    }
}
