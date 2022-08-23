package ast;

import java.util.ArrayList;

import ArmazenaVariaveis.*;
import Exceptions.*;
import Parser.Token;

public class ComandoSe extends AbstractCommand{

    private String cond;
    private String tipo;
    private ArrayList<AbstractCommand> ListTrue;
    private ArrayList<AbstractCommand> ListFalse;
    private ArrayList<IsiVariable> listVar; // Contém uma lista de variáveis auxiliares em ordem para calcular o valor atribuído
    private IsiVarTable tblvar;

    public ComandoSe(ArrayList<AbstractCommand> ListTrue, ArrayList<AbstractCommand> ListFalse, ArrayList<IsiVariable> listVar, IsiVarTable tblvar, String tipo){
        this.ListTrue = ListTrue;
        this.ListFalse = ListFalse;
        this.listVar = listVar;
        this.tblvar = tblvar;
        this.cond = "";
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
        
        for(AbstractCommand cmd: ListTrue){
            cmd.executa();
        }

        IsiVarTable tblSe = new IsiVarTable();
        for (IsiVariable v : tblvar.getAll()) {
            if(!v.getName().startsWith("aux_")){
                IsiVariable v2 = new IsiVariable(v.getName(), v.getType());
                v2.setValor(v.getValor());
                tblSe.add(v2);
            }
            tblSe.add(v);
        }

        ArrayList<String> listSe = new ArrayList<String>();
        for (IsiVariable v : tblAnt.getAll()) {
            if(!v.getName().startsWith("aux_")){
                if(v.getValor() != null && tblSe.get(v.getName()).getValor() != null){
                    if(!v.getValor().equals(tblSe.get(v.getName()).getValor())){
                        listSe.add(v.getName());
                    }
                }else{
                    if(v.getValor() == null && tblSe.get(v.getName()).getValor() != null){
                        listSe.add(v.getName());
                    }
                    if(v.getValor() != null && tblSe.get(v.getName()).getValor() == null){
                        listSe.add(v.getName());
                    }
                }
            }
        }

        for (IsiVariable v : tblAnt.getAll()) {
            if (!v.getName().startsWith("aux_")) {
                tblvar.get(v.getName()).setValor(v.getValor());
            }
        }

        if(!ListFalse.isEmpty()){
            for(AbstractCommand cmd: ListFalse){
                cmd.executa();
            }

            IsiVarTable tblSenao = new IsiVarTable();
            for (IsiVariable v : tblvar.getAll()) {
                if(!v.getName().startsWith("aux_")){
                    IsiVariable v2 = new IsiVariable(v.getName(), v.getType());
                    v2.setValor(v.getValor());
                    tblSenao.add(v2);
                }
                tblSenao.add(v);
            }

            ArrayList<String> listSenao = new ArrayList<String>();
            for (IsiVariable v : tblAnt.getAll()) {
                if(!v.getName().startsWith("aux_")){
                    if(v.getValor() != null && tblSenao.get(v.getName()).getValor() != null){
                        if(!v.getValor().equals(tblSenao.get(v.getName()).getValor())){
                            listSenao.add(v.getName());
                        }
                    }else{
                        if(v.getValor() == null && tblSenao.get(v.getName()).getValor() != null){
                            listSenao.add(v.getName());
                        }
                        if(v.getValor() != null && tblSenao.get(v.getName()).getValor() == null){
                            listSenao.add(v.getName());
                        }
                    }
                }
            }

            // Verifica se uma variável é inicializada tanto no Se como no Senão.
            // Usado para verificar se a variável foi ou não inicializada antes de utiliza-lá
            for (String lSe : listSe) {
                if(listSenao.contains(lSe)){
                    if(tblSe.get(lSe).getValor().equals(tblSenao.get(lSe).getValor())){
                        tblvar.get(lSe).setValor(tblSe.get(lSe).getValor());
                    }else{
                        tblvar.get(lSe).setValor("CmdSe");
                    }
                }else{
                    tblvar.get(lSe).setValor(tblAnt.get(lSe).getValor());
                }
            }

            for (String lSenao : listSenao) {
                if(!listSe.contains(lSenao)){
                    tblvar.get(lSenao).setValor(tblAnt.get(lSenao).getValor());
                }
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

        String condJava = cond;
        if(tipo == "Texto"){
            if (cond.contains("==")) {
                condJava = condJava.replace("==", ".equals(") + ")";  
            } else if (cond.contains("!=")) {
                condJava = "!" + condJava.replace("!=", ".equals(") + ")";
            }
        }
		str.append("if ("+condJava+") {\n");
        GeraCodigo.tabsJava++;
        for(AbstractCommand cmd: ListTrue){
            str.append(cmd.generateJavaCode());
        }

        GeraCodigo.tabsJava--;
        tab = 0;
        while(tab < GeraCodigo.tabsJava){
            str.append("    ");
            tab++;
        }
        
        if(!ListFalse.isEmpty()){
            str.append("} else {\n");
            GeraCodigo.tabsJava++;
            for(AbstractCommand cmd: ListFalse){
                str.append(cmd.generateJavaCode());
            }

            GeraCodigo.tabsJava--;
            tab = 0;
            while(tab < GeraCodigo.tabsJava){
                str.append("    ");
                tab++;
            }

            str.append("}\n");
        }else{
            str.append("}\n");
        }
        
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
        if(tipo.equals("Texto")){
            if (cond.contains("==")) {
                condC = "strcmp(" + condC.replace("==", ",") + ") == 0";  
            } else if (cond.contains("!=")) {
                condC = "strcmp(" + condC.replace("!=", ",") + ") != 0";
            }
        }
		str.append("if ("+condC+") {\n");
        GeraCodigo.tabsC++;
        for(AbstractCommand cmd: ListTrue){
            str.append(cmd.generateCCode());
        }
        
        GeraCodigo.tabsC--;
        tab = 0;
        while(tab < GeraCodigo.tabsC){
            str.append("    ");
            tab++;
        }

        if(!ListFalse.isEmpty()){
            str.append("} else {\n");
            GeraCodigo.tabsC++;
            for(AbstractCommand cmd: ListFalse){
                str.append(cmd.generateCCode());
            }
            GeraCodigo.tabsC--;
            tab = 0;
            while(tab < GeraCodigo.tabsC){
                str.append("    ");
                tab++;
            }

            str.append("}\n");
        }else{
            str.append("}\n");
        }
        
        return str.toString();
    }

    @Override
	public String toString() {
		return "Cmd Se!\n";
	}
    
}
