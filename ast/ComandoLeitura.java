package ast;

import ArmazenaVariaveis.*;

public class ComandoLeitura extends AbstractCommand{
    
    private String id;
    private int    tipo;
    private IsiVarTable tblvar;
    
    public ComandoLeitura(String id, int tipo, IsiVarTable tblvar) {
        this.id = id;
        this.tipo = tipo;
        this.tblvar = tblvar;
    }
    
    @Override
	public void executa() {
        tblvar.get(id).setValor("CmdLeitura");
	}

    @Override
    public String generateJavaCode() {
        StringBuilder str = new StringBuilder();
        int tab = 0;
        while(tab < GeraCodigo.tabsJava){
            str.append("    ");
            tab++;
        }

        if (tipo == IsiVariable.intNum) {
            str.append(id +" = _key.nextInt();\n");
            return str.toString();
        }else if (tipo == IsiVariable.doubleNum){
            str.append(id +" = _key.nextDouble();\n");
            return str.toString();
        }else{
            str.append(id +" = _key.nextLine();\n");
            return str.toString();
        }
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
            str.append("scanf(\"%d\", &" + id + ");\n");
            return str.toString();
        }else if (tipo == IsiVariable.doubleNum){
            str.append("scanf(\"%lf\", &" + id + ");\n");
            return str.toString();
        }else{
            str.append("scanf(\"%s\", &" + id + ");\n");
            return str.toString();
        }
    }

    @Override
	public String toString() {
		return "Cmd Leia!\n";
	}
}
