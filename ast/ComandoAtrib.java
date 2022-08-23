package ast;

import java.util.ArrayList;
import ArmazenaVariaveis.*;
import Exceptions.IsiSemanticException;
import Parser.Token;

public class ComandoAtrib extends AbstractCommand{
    
    private String nomeVar;
    private int tipo;
    private String expr;
    private IsiVarTable tblvar;
    private ArrayList<IsiVariable> listVar; // Contém uma lista de variáveis auxiliares em ordem para calcular o valor atribuído
    
    public ComandoAtrib(String nomeVar, int tipo, ArrayList<IsiVariable> listVar, IsiVarTable tblvar) {
        this.nomeVar = nomeVar;
        this.tipo = tipo;
        this.listVar = listVar;
        this.tblvar = tblvar;
        this.expr = "";
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
        
        tblvar.get(nomeVar).setValor(listVar.get(listVar.size()-1).getName());
	}
    
    @Override
    public String generateJavaCode() {
        StringBuilder str = new StringBuilder();
        int tab = 0;
        while(tab < GeraCodigo.tabsJava){
            str.append("    ");
            tab++;
        }
        str.append(nomeVar + " = "+expr.replace(',', '.')+";\n");
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
        if (tipo == IsiVariable.intNum || tipo == IsiVariable.doubleNum){
            str.append(nomeVar + " = "+expr.replace(',', '.')+";\n");
            return str.toString();
        }else{
            str.append("strcpy(" + nomeVar + ", " + expr +");\n");
            return str.toString();
        }  
    }

    @Override
	public String toString() {
		return "Cmd Atribuição!\n";
	}
}
