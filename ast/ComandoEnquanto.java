package ast;

import java.util.ArrayList;

import ArmazenaVariaveis.*;
import Exceptions.*;
import Parser.Token;
public class ComandoEnquanto extends AbstractCommand{

    private String cond;
    private ArrayList<AbstractCommand> ListWhile;
    private ArrayList<IsiVariable> listVar;
    private IsiVarTable tblvar;
    private String tipo;

    public ComandoEnquanto(ArrayList<AbstractCommand> ListWhile, ArrayList<IsiVariable> listVar, IsiVarTable tblvar, String tipo){
        this.ListWhile = ListWhile;
        this.listVar = listVar;
        this.tblvar = tblvar;
        this.tipo = tipo;
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
            cond = exp;
            if (exp != "") {
                variavel.setValor(exp);
                exp = "";
            }
        }

        IsiVarTable tblAnt = new IsiVarTable();
        for (IsiVariable v : tblvar.getAll()) {
            if(!v.getName().startsWith("aux_")){
                IsiVariable v2 = new IsiVariable(v.getName(), v.getType());
                v2.setValor(v.getValor());
                tblAnt.add(v2);
            }
        }

        for(AbstractCommand cmd: ListWhile){
            cmd.executa();
        }

        for (IsiVariable v : tblAnt.getAll()) {
            if (!v.getName().startsWith("aux_")) {
                tblvar.get(v.getName()).setValor(v.getValor());
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
        GeraCodigo.tabsJava++;
        String condJava = cond;
        if(tipo == "Texto"){
            if (cond.contains("==")) {
                condJava = condJava.replace("==", ".equals(") + ")";  
            } else if (cond.contains("!=")) {
                condJava = "!" + condJava.replace("!=", ".equals(") + ")";
            }
        }
		str.append("while ("+condJava+") {\n");
        for(AbstractCommand cmd: ListWhile){   
            str.append(cmd.generateJavaCode());
        }

        GeraCodigo.tabsJava--;
        tab = 0;
        while(tab < GeraCodigo.tabsJava){
            str.append("    ");
            tab++;
        }

        str.append("}\n");
        
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
        String condC = cond;
        if(tipo == "Texto"){
            if (cond.contains("==")) {
                condC = "strcmp(" + condC.replace("==", ",") + ") == 0";  
            } else if (cond.contains("!=")) {
                condC = "strcmp(" + condC.replace("!=", ",") + ") != 0";
            }
        }
		str.append("while ("+condC+") {\n");
        GeraCodigo.tabsC++;
        for(AbstractCommand cmd: ListWhile){
            str.append(cmd.generateCCode()+"");
        }
        GeraCodigo.tabsC--;
        tab = 0;
        while(tab < GeraCodigo.tabsC){
            str.append("    ");
            tab++;
        }
        str.append("}\n");
        
        return str.toString();
    }

    @Override
	public String toString() {
		return "Cmd Enquanto!\n";
	}
    
}
