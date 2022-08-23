package ArmazenaVariaveis;

import java.util.ArrayList;

import Parser.Token;

public class IsiVariable {
    public static final int intNum = 0;
	public static final int doubleNum = 1;
	public static final int TEXT = 2;
	
    private String name;
	private int type;
	private String valor;
	private ArrayList<Token> listTk;
	private int utilizada;
	private static int contAuxVar = 0;
	
	public IsiVariable(String name, int type) {
		this.name = name;
		this.type = type;
		this.utilizada = 0;
	}

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public ArrayList<Token> getListTk() {
		return listTk;
	}

	public void setListTk(ArrayList<Token> listTk) {
		this.listTk = listTk;
		if(name.startsWith("aux_")){
			contAuxVar++;
		}
	}

	public static int getContAuxVar (){
		return contAuxVar;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public int getUtilizada() {
		return utilizada;
	}

	public void setUtilizada() {
		this.utilizada = 1;
	}

	@Override
	public String toString() {
		//return "IsiVariable [name=" + name + ", type=" + type + ", expre=" + expr + "]";
		return name + "=" + valor;
	}

}
