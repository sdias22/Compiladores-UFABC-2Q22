package ast;

import ArmazenaVariaveis.IsiVarTable;
import ArmazenaVariaveis.IsiVariable;

public class DeclaraGCod extends AbstractCommand{
    private IsiVarTable tbl;

    public DeclaraGCod(IsiVarTable tbl){
        this.tbl = tbl;
    }

	@Override
	public void executa() {
		
	}

    public String generateJavaCode () {
		StringBuilder str = new StringBuilder();
		String intN = "";
		String doub = "";
		String text = "";
		int contNum = 0;
		int contText = 0;
		int contDoub = 0;
		for(IsiVariable var : tbl.getAll()){
			if(!var.getName().startsWith("aux_")){
				if (var.getType() == 0 ) {
					if(contNum != 0){
						intN += ", ";
					}
					intN += var.getName();
					contNum++;
				}else if(var.getType() == IsiVariable.doubleNum){
					if(contDoub != 0){
						doub += ", ";
					}
					doub += var.getName();
					contDoub++;
				}else{
					if(contText != 0){
						text += ", ";
					}
					text += var.getName();
					contText++;
				}
			}
        }
		
		if (!intN.equals("")) {
			int tab = 0;
        	while(tab < GeraCodigo.tabsJava){
				str.append("    ");
				tab++;
        	}
			str.append("int "+intN+";\n");
		}

		if (!doub.equals("")) {
			int tab = 0;
        	while(tab < GeraCodigo.tabsJava){
				str.append("    ");
				tab++;
        	}
			str.append("double "+doub+";\n");
		}
		
		if (!text.equals("")) {
			int tab = 0;
        	while(tab < GeraCodigo.tabsJava){
				str.append("    ");
				tab++;
        	}
			str.append("String "+text+";\n");
		}

		return str.toString();
	}

	public String generateCCode () {
		StringBuilder str = new StringBuilder();
		String intN = "";
		String doub = "";
		int contNum = 0;
		int contDoub = 0;
		for(IsiVariable var : tbl.getAll()){
			if(!var.getName().startsWith("aux_")){
				if (var.getType() == IsiVariable.intNum ) {
					if(contNum != 0){
						intN += ", ";
					}
					intN += var.getName();
					contNum++;
				}else if(var.getType() == IsiVariable.doubleNum){
					if(contDoub != 0){
						doub += ", ";
					}
					doub += var.getName();
					contDoub++;
				}else{
					int tab = 0;
					while(tab < GeraCodigo.tabsC){
						str.append("    ");
						tab++;
					}
					str.append("char " + var.getName()+"[100] = \" \";\n");
				}
			}
        }

		if (!intN.equals("")) {
			int tab = 0;
			while(tab < GeraCodigo.tabsC){
				str.append("    ");
				tab++;
			}
			str.append("int "+intN+";\n");
		}

		if (!doub.equals("")) {
			int tab = 0;
			while(tab < GeraCodigo.tabsC){
				str.append("    ");
				tab++;
			}
			str.append("double "+doub+";\n");
		}

		return str.toString();
	}

	@Override
	public String toString() {
		return "Declara!\n";
	}
}