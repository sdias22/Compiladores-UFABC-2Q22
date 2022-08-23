package ast;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class GeraCodigo {
	private ArrayList<AbstractCommand> comandos;
	
	// Auxiliares para gerar os códigos em Java e C.
	public static int tabsJava = 2;
	public static int tabsC = 1;

	public GeraCodigo (ArrayList<AbstractCommand> cmd){
		this.comandos = cmd;
	}

	public void executa (){
		for (AbstractCommand command: comandos) {
			command.executa();
		}
	}

    public void javaCod (){
        StringBuilder str = new StringBuilder();
		str.append("import java.util.Scanner;\n");
		str.append("public class MainClass{ \n");
		str.append("	public static void main(String args[]){\n ");
		str.append("		Scanner _key = new Scanner(System.in);\n");

        for (AbstractCommand command: comandos) {
			str.append(command.generateJavaCode());
		}

		str.append("	}\n");
		str.append("}");

        try {
			FileWriter fr = new FileWriter(new File("CodGerado/MainClass.java"));
			fr.write(str.toString());
			fr.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

    }

    public void cCod () {
        StringBuilder str = new StringBuilder();
        str.append("#include <stdio.h>\n");
		str.append("#include <string.h>\n");
        str.append("int main(void){\n");

        for (AbstractCommand command: comandos) {
			str.append(command.generateCCode());
		}

        str.append("}");

        try {
			FileWriter fr = new FileWriter(new File("CodGerado/main.c"));
			fr.write(str.toString());
			fr.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

    }

    public void setComandos(ArrayList<AbstractCommand> comandos) {
		this.comandos = comandos;
	}

}