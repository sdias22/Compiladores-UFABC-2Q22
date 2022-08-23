package ast;

import java.util.ArrayList;

import ArmazenaVariaveis.IsiVarTable;
import ArmazenaVariaveis.IsiVariable;
import Parser.Token;

public class ComandoEscolha extends AbstractCommand{

    private String var;
    private String nomevar;
    private ArrayList<ArrayList<AbstractCommand>> listCmdCasos;
    private IsiVarTable tblvar;
    private ArrayList<IsiVariable> listVar;
    private ArrayList<String> listCaso;

    public ComandoEscolha(ArrayList<ArrayList<AbstractCommand>> listCmdCasos, ArrayList<IsiVariable> listVar, IsiVarTable tblvar){
        this.listCmdCasos = listCmdCasos;
        this.tblvar = tblvar;
        this.listVar = listVar;
        listCaso = new ArrayList<String>();
    }

    public void gListCaso(){
        int cont = 0;
        for (IsiVariable variavel : listVar) {
            String caso = "";
            for (Token tk : variavel.getListTk()) {
                if(!tk.getText().startsWith("aux_")){
                    if (cont == 0) {
                        nomevar = tk.getText();
                        var = tblvar.get(tk.getText()).getValor();
                        cont++;
                    }else{
                        if(!tk.getText().equals("|")){
                            caso += tk.getText();
                        }
                        
                    }
                }
            }
            if (caso != "") listCaso.add(caso);
            caso = "";
            
        }
    }

    @Override
	public void executa() {
        gListCaso();
        for(ArrayList<AbstractCommand> casos: listCmdCasos){
            for(AbstractCommand cmd: casos){
                if(cmd != null){
                    cmd.executa();
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

        str.append("switch ("+nomevar+") {\n");
        int Contcaso = 0;
        GeraCodigo.tabsJava++;
        for(ArrayList<AbstractCommand> casos: listCmdCasos){
            String caso = listCaso.get(Contcaso);
            Contcaso++;

            tab = 0;
            while(tab < GeraCodigo.tabsJava){
                str.append("    ");
                tab++;
            }

            if(caso.equals("semcaso")){
                str.append("default:\n");
            }else{
                str.append("case " + caso + " :\n");
            }
            GeraCodigo.tabsJava++;
            for(AbstractCommand cmd: casos){
                if(cmd != null){
                    str.append(cmd.generateJavaCode());
                }
            }

            GeraCodigo.tabsJava--;
            tab = 0;
            while(tab < GeraCodigo.tabsJava){
                str.append("    ");
                tab++;
            }

            str.append("break;\n");
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
        if (var.startsWith("aux_")) {
            var = tblvar.get(var).getValor().replace("\"", "");
        }
        int tipo = tblvar.get(nomevar).getType();

        int tab = 0;
        while(tab < GeraCodigo.tabsC){
            str.append("    ");
            tab++;
        }
        
        if(tipo == IsiVariable.TEXT){
            String varTxt = "";
            int c = 0;
            while (c < 100) {
                if(c != 0) varTxt+= "+";
                varTxt += nomevar +"["+ c + "]*(10^"+c+")";
                c++;
            }
            str.append("switch ("+varTxt+") {\n");
        }else{
            str.append("switch ("+nomevar+") {\n");
        }

        int Contcaso = 0;
        GeraCodigo.tabsC++;
        for(ArrayList<AbstractCommand> casos: listCmdCasos){
            String caso = listCaso.get(Contcaso);
            Contcaso++;

            tab = 0;
            while(tab < GeraCodigo.tabsC){
                str.append("    ");
                tab++;
            }

            if(caso.equals("semcaso")){
                str.append("default:\n");
            }else{
                if(tipo == IsiVariable.TEXT){
                    String cS = caso.replace("\"", "");
                    String casoTxt = "";
                    int c = 0;
                    while (c < cS.length()) {
                            if(c != 0) casoTxt+= "+";
                            casoTxt += "\'"+ cS.charAt(c) + "\'*(10^" + c +")"; 
                            c++;
                    }
                    str.append("case (" + casoTxt + ") :\n");
                }else{
                    str.append("case " + caso + " :\n");
                }
            }
            GeraCodigo.tabsC++;
            for(AbstractCommand cmd: casos){
                if(cmd != null){
                    str.append(cmd.generateCCode());
                }
            }
            
            GeraCodigo.tabsC--;
            tab = 0;
            while(tab < GeraCodigo.tabsC){
                str.append("    ");
                tab++;
            }

            str.append("break;\n");
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
		return "Cmd Escolha!\n";
	}
    
}
