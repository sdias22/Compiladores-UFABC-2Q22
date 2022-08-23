package ast;

import java.util.ArrayList;
import ArmazenaVariaveis.*;
import Exceptions.*;
import Parser.Token;

public class ComandoEscrita extends AbstractCommand{
    
    private int    tipo;
    private String expr;
    private ArrayList<IsiVariable> listVar;
    private IsiVarTable tblvar;
    
    public ComandoEscrita(int tipo, ArrayList<IsiVariable> listVar, IsiVarTable tblvar) {
        this.tipo = tipo;
        this.listVar = listVar;
        this.tblvar = tblvar;
    }
    
    @Override
	public void executa() {
        for (IsiVariable variavel : listVar) {
            String exp = "";
            for (Token tk : variavel.getListTk()) {
                if (tk.getType() == Token.TK_Variavel) {
                    if(tblvar.get(tk.getText()).getValor() == null){
                        throw new IsiSemanticException("Variável sem valor! Encontrado "+Token.TK_TEXT[tk.getType()]+" ("+tk.getText()+") at Linha "+tk.getLine()+" and Coluna "+tk.getColumn());
                    }else{
                        if(!tblvar.get(tk.getText()).getName().startsWith("aux_")){
                            exp += tk.getText();
                        }else{
                            exp += tblvar.get(tk.getText()).getValor();
                        }
                    }
                }else {
                    exp += tk.getText();
                }
            }
            expr = exp;
            if (exp != "") {
                variavel.setValor(exp);
                exp = "";
            }
        }
    }

    @Override
    public String generateJavaCode() {
        StringBuilder str = new StringBuilder();
        int tab = 0;
        while(tab < GeraCodigo.tabsJava){
            str.append("    ");
            tab++;
        }
        str.append("System.out.println("+expr+");\n");
        return str.toString();
    }

    @Override
    public String generateCCode() {
        StringBuilder str = new StringBuilder();
        int tab = 0;
        while(tab < GeraCodigo.tabsC){
            str.append("    ");
            tab++;
        }

        if (tipo == IsiVariable.intNum) {
            str.append("printf(\"%d\\n\", " + expr+ ");\n");
            return str.toString();
        }else if (tipo == IsiVariable.doubleNum){
            str.append("printf(\"%.2lf\\n\", (double)" + expr + ");\n");
            return str.toString();
        }else{
            str.append("printf(\"%s\\n\", " + expr + ");\n");
            return str.toString();
        }
    }

    @Override
	public String toString() {
		return "Cmd Escreva!\n";
	}
}
