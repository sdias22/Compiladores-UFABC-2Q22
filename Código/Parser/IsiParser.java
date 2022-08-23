package Parser;
import Exceptions.*;
import ast.*;
import ArmazenaVariaveis.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IsiParser{
    private IsiScanner scanner; // analisador léxico
	private Token      token;   // o token atual
    private IsiVarTable tblVar = new IsiVarTable(); // armazena as variaveis
    private IsiVariable var; // auxiliar, guarda as infos. da variavel
    private String varNome; // auxiliar, guarda o nome da variavel
    private ArrayList<AbstractCommand> commands = new ArrayList<AbstractCommand>();
    private ArrayList<AbstractCommand> listCerta = commands;
    private GeraCodigo program;
    private ArrayList<Token> listTokens;
    private ArrayList<IsiVariable> listVarAux;
    
    // Lista de comandos
    private final List <String> TK_Cmd = new ArrayList<>(Arrays.asList("leia", "escreva", "enquanto", "se", "escolha"));

    // Verifica e retorna quais variáveis não foram utilizadas
    public void varSemUso () {
        String SemUso = "";
        for(IsiVariable varG : tblVar.getAll()){
            if(!varG.getName().startsWith("aux_")){
                if (varG.getUtilizada() == 0) {
                    SemUso += "[" + varG.getName() + "] ";
                }
            }
        }
        if (SemUso != ""){
            System.out.println("Warning - Variaveis não utilizadas:" + SemUso);
        }
    }

    public void geraCodJava () {
        program.javaCod();
    }

    public void geraCodC () {
        program.cCod();
    }

	public IsiParser(IsiScanner scanner) {
		this.scanner = scanner;
	}

    // Atualiza para o próximo Token
    public void nextTkn(){
        token = scanner.nextToken();
    }

    public void prog() {
        nextTkn();
        if(token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("programa")){
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'programa'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
        declara();
        DeclaraGCod decl = new DeclaraGCod(tblVar);
        commands.add(decl);
        bloco();
        fimProg();
        program = new GeraCodigo(commands);
        program.executa();
    }

    // Considera que pode ter nenhuma ou várias linhas de declare.
    public void declara () {
        nextTkn();
        if(token.getType() == Token.TK_PResarvada && token.getText().toLowerCase().equals("declare")){       
            int tipo = tipoDado();
            variavel();
            varNome = token.getText();
            if(!tblVar.exists(varNome)){
                var = new IsiVariable(varNome, tipo);
                tblVar.add(var);
            }else{
                throw new IsiSemanticException("Variável "+varNome+" já declarada");
            }
            nextTkn();
            while(token.getType() != Token.TK_Ponto){
                virgula();
                variavel();
                varNome = token.getText();
                if(!tblVar.exists(varNome)){
                    var = new IsiVariable(varNome, tipo);
                    tblVar.add(var);
                }else{
                    throw new IsiSemanticException("Variável "+varNome+" já declarada");
                }
                nextTkn();
            }
            declara();
        }
    }

    // Responsável por chamar a função de cmd que preenche o bloco até encontrar o fim do programa
    public void bloco () {
        if((token.getType() == Token.TK_Variavel || token.getType() == Token.TK_PResarvada) && !token.getText().toLowerCase().equals("fimprog")){
            cmd();
            nextTkn();
            bloco();
        }
    }

    // Chama a função responsável para cada comando encontrado
    public void cmd () {
        if (token.getType() == Token.TK_Variavel) {
            verificaVar(varNome);
            cmdExpr(token.getText(),tblVar.get(token.getText()).getType());
        }else if (token.getType() != Token.TK_PResarvada || !(TK_Cmd.contains(token.getText()))) {
            throw new IsiSyntaxException("Esperava-se uma palavra reservada referente a um comando!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else {
            switch (token.getText().toLowerCase().toString()) {
                case "leia":
                    cmdLeitura();
                    break;
                case "escreva":
                    cmdEscrita();
                    break;
                case "se":
                    cmdSe();
                    break;
                case "enquanto":
                    cmdEnquanto();
                    break;
                case "escolha":
                    cmdEscolha();
                    break;
            }
        }
    }

    // Comando de Atribuição
    public void cmdExpr (String nome, int tipo) {
        opAtrib();

        ArrayList<Token> ltok = new ArrayList<Token>();
        listTokens = ltok;
        ArrayList<IsiVariable> lvar = new ArrayList<IsiVariable>();
        listVarAux = lvar;

        vAtribuido(tipo);

        if(tipo != IsiVariable.TEXT){
            divExpr(tipo);
        }

        ponto();

        listTokens = null;
        lvar = listVarAux;
        listVarAux = null;

        tblVar.get(nome).setUtilizada();

        ComandoAtrib cmd = new ComandoAtrib(nome, tipo, lvar, tblVar);
        listCerta.add(cmd);
    }

    // Comando Se
    public void cmdSe () {
        nextTkn();
        AP();

        ArrayList<IsiVariable> lvar = new ArrayList<IsiVariable>();
        listVarAux = lvar;

        String tipo = condicao();

        listTokens = null;
        lvar = listVarAux;
        listVarAux = null;

        FP();
        nextTkn();
        entao();
        ArrayList<AbstractCommand> listAnt = listCerta;
        ArrayList<AbstractCommand> listTrue = new ArrayList<AbstractCommand>();
        listCerta = listTrue;
        comandos();
        ArrayList<AbstractCommand> listFalse = new ArrayList<AbstractCommand>();
        cmdSenao(listFalse);
        fimSe();
        listCerta = listAnt;

        ComandoSe cmd = new ComandoSe(listTrue, listFalse, lvar, tblVar, tipo);
        listCerta.add(cmd);
    }

    // Comando Senão
    public void cmdSenao(ArrayList<AbstractCommand> listFalse){
        if(token.getType() == Token.TK_PResarvada && token.getText().toLowerCase().equals("senao")){
            listCerta = listFalse;
            comandos();
        }
    }

    // Comando de Leitura
    public void cmdLeitura () {
        nextTkn();
        AP();
        variavel();
        verificaVar(token.getText());

        tblVar.get(token.getText()).setUtilizada();

        ComandoLeitura cmd = new ComandoLeitura(token.getText(), tblVar.get(token.getText()).getType(),tblVar);
        listCerta.add(cmd);

        nextTkn();
        FP();
        nextTkn();
        ponto();
    }

    // Comando de Escrita
    public void cmdEscrita () {
        nextTkn();
        AP();
        
        ArrayList<Token> ltok = new ArrayList<Token>();
        listTokens = ltok;
        ArrayList<IsiVariable> lvar = new ArrayList<IsiVariable>();
        listVarAux = lvar;

        int tipo;

        nextTkn();
        if(token.getType() == Token.TK_IntNumero || token.getType() == Token.TK_DoubleNumero ||  (token.getType() == Token.TK_Variavel && (tblVar.get(token.getText()).getType() == IsiVariable.intNum || tblVar.get(token.getText()).getType() == IsiVariable.doubleNum))){
            if(token.getType() == Token.TK_Variavel) verificaVar(token.getText());
            listTokens.add(token);
            conta(1);
            tipo = IsiVariable.doubleNum;
            divExpr(tipo);
        }else if(token.getType() == Token.TK_Texto || (token.getType() == Token.TK_Variavel && tblVar.get(token.getText()).getType() == IsiVariable.TEXT)){
            if(token.getType() == Token.TK_Variavel) verificaVar(token.getText());
            tipo = IsiVariable.TEXT;
            IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), tipo);
            ArrayList<Token> lTk = new ArrayList<Token>();
            lTk.add(token);
            v.setListTk(lTk);
            tblVar.add(v);
            listVarAux.add(v);

            nextTkn();
        }else {
            throw new IsiSyntaxException("Esperava-se uma variável, número ou texto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
        FP();

        nextTkn();
        ponto();

        listTokens = null;
        lvar = listVarAux;
        listVarAux = null;

        ComandoEscrita cmd = new ComandoEscrita(tipo, lvar, tblVar);
        listCerta.add(cmd);
    }

    // Comando Enquanto (While)
    public void cmdEnquanto () {
        nextTkn();
        AP();

        ArrayList<IsiVariable> lvar = new ArrayList<IsiVariable>();
        listVarAux = lvar;

        String tipo = condicao();

        listTokens = null;
        lvar = listVarAux;
        listVarAux = null;

        FP();
        nextTkn();
        faca();

        ArrayList<AbstractCommand> listAnt = listCerta;
        ArrayList<AbstractCommand> listWhile = new ArrayList<AbstractCommand>();
        listCerta = listWhile;

        comandos();
        fimEnquanto();
        listCerta = listAnt;
        ComandoEnquanto cmd = new ComandoEnquanto(listWhile, lvar, tblVar, tipo);
        listCerta.add(cmd);
    }
    
    // Comando Escolha (Switch)
    public void cmdEscolha () {
        nextTkn();
        AP();
        variavel();
        verificaVar(token.getText());

        if(tblVar.get(token.getText()).getType() == IsiVariable.doubleNum){
            throw new IsiSemanticException("Variável do tipo double não é aceita no cmdEscolha! "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }

        ArrayList<IsiVariable> lvar = new ArrayList<IsiVariable>();
        listVarAux = lvar;

        ArrayList<Token> ltok = new ArrayList<Token>();
        listTokens = ltok;
        listVarAux = lvar;

        IsiVariable v = tblVar.get(token.getText());

        ArrayList<Token> lTk = new ArrayList<Token>();
        lTk.add(token);
        v.setListTk(lTk);
        tblVar.add(v);
        listVarAux.add(v);
        lvar = listVarAux;

        int tipo = tblVar.get(token.getText()).getType();

        nextTkn();
        FP();
        nextTkn();
        faca();

        ArrayList<AbstractCommand> listAnt = listCerta;
        ArrayList<ArrayList<AbstractCommand>> listCmdCasos = new ArrayList<ArrayList<AbstractCommand>>();

        casos(listCmdCasos ,tipo, lvar);
        
        semCaso(listCmdCasos,lvar);
        listTokens = null;
        lvar = listVarAux;
        listVarAux = null;
        fimEscolha();

        listCerta = listAnt;
        ComandoEscolha cmd = new ComandoEscolha(listCmdCasos, lvar, tblVar);
        listCerta.add(cmd);
    }

    // Verifica os casos do comando Escolha
    public void casos(ArrayList<ArrayList<AbstractCommand>> listCmdCasos , int tipo, ArrayList<IsiVariable> lvar){
        nextTkn();
        if(token.getType() == Token.TK_PResarvada && token.getText().toLowerCase().equals("caso")){
            caso();

            ArrayList<Token> ltok = new ArrayList<Token>();
            listTokens = ltok;
            listVarAux = lvar;

            DifVar(tipo);

            IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), tipo);
            ArrayList<Token> lTk = new ArrayList<Token>();
            lTk.add(token);
            v.setListTk(lTk);
            tblVar.add(v);
            listVarAux.add(v);
            lvar = listVarAux;

            lvar = listVarAux;

            nextTkn();
            faca();
            ArrayList<AbstractCommand> listCasos = new ArrayList<AbstractCommand>();
            listCerta = listCasos;
            comandos();

            listVarAux = lvar;

            fimCaso();
            listCmdCasos.add(listCerta);
            casos(listCmdCasos ,tipo, lvar);
        }
    }

    // Verificar se tem a condição caso não tenha um caso específico no comando Escolha
    public void semCaso(ArrayList<ArrayList<AbstractCommand>> listCmdCasos, ArrayList<IsiVariable> lvar){
        if(token.getType() == Token.TK_PResarvada && token.getText().toLowerCase().equals("semcaso")){
            ArrayList<Token> ltok = new ArrayList<Token>();
            listTokens = ltok;
            listVarAux = lvar;

            IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), IsiVariable.TEXT);
            ArrayList<Token> lTk = new ArrayList<Token>();
            lTk.add(token);
            v.setListTk(lTk);
            tblVar.add(v);
            listVarAux.add(v);
            lvar = listVarAux;

            nextTkn();
            faca();
            ArrayList<AbstractCommand> listCasos = new ArrayList<AbstractCommand>();
            listCerta = listCasos;
            comandos();

            listVarAux = lvar;

            fimCaso();
            listCmdCasos.add(listCasos);
            nextTkn();
        }
    }

    // Verifica se é o fim do programa
    public void fimProg () {
        if(token.getType() != Token.TK_PResarvada){
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimprog'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else if(!token.getText().toLowerCase().equals("fimprog")){
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimprog'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else{
            nextTkn();
            ponto();
        }
    }

    //Chama cmd() para preencher os comandos que estão dentro de outros (IF)
    public void comandos(){
        nextTkn();
        if((token.getType() == Token.TK_PResarvada && TK_Cmd.contains(token.getText().toLowerCase())) || token.getType() == Token.TK_Variavel){
            cmd();
            comandos();
        }
    }

    // Verifica o valor a ser atribuido a uma variável e se no cmd escolha verifica se a variavel pode ter aquele valor em um caso
    public void vAtribuido (int tipo) {
        nextTkn();
        if(token.getType() == Token.TK_Variavel){
            verificaVar(token.getText());
        }
        if(token.getType() == Token.TK_IntNumero || token.getType() == Token.TK_DoubleNumero || (token.getType() == Token.TK_Variavel && (tblVar.get(token.getText()).getType() == IsiVariable.doubleNum || tblVar.get(token.getText()).getType() == IsiVariable.intNum ))){
            if(tipo == IsiVariable.intNum || tipo == IsiVariable.doubleNum){
                if(tipo == IsiVariable.intNum && (token.getType() == Token.TK_DoubleNumero || (token.getType() == Token.TK_Variavel && tblVar.get(token.getText()).getType() == IsiVariable.doubleNum))){
                    throw new IsiSemanticException("Variável do tipo inteiro não atribuí o tipo Double! "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
                }
                listTokens.add(token);
                conta(tipo);
            }else{
                throw new IsiSemanticException("Valor não condizente com o tipo da variável (Texto)! "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
            }
        }else if(token.getType() == Token.TK_Texto || (token.getType() == Token.TK_Variavel && tblVar.get(token.getText()).getType() == 1)){
            if(tipo == IsiVariable.TEXT){

                IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), tipo);
                ArrayList<Token> lTk = new ArrayList<Token>();
                lTk.add(token);
                v.setListTk(lTk);
                tblVar.add(v);
                listVarAux.add(v);

                nextTkn();
            }else{
                throw new IsiSemanticException("Valor não condizente com o tipo da variável (Número)! "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
            }
        }else{
            throw new IsiSyntaxException("Esperava-se uma variável, número ou texto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica a expressão
    public void conta(int tipo){
        nextTkn();
        if(token.getType() != Token.TK_Ponto && token.getType() != Token.TK_OpRel && token.getType() != Token.TK_FechaParentes && !token.getText().toLowerCase().equals( "entao") && !token.getText().toLowerCase().equals( "faca")){
            opArit();
            listTokens.add(token);
            num();
            if(tipo == IsiVariable.intNum){
                if(token.getType() == Token.TK_DoubleNumero || (token.getType() == Token.TK_Variavel && tblVar.get(token.getText()).getType() == IsiVariable.doubleNum)){
                    throw new IsiSemanticException("Variável do tipo inteiro não atribuí o tipo Double! "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
                }
            }
            listTokens.add(token);
            conta(tipo);
        }
    }

    // Divide a expressão em variáveis para realizar as contas na ordem correta
    public void divExpr (int tipo){
        int pos = 0;
        while(listTokens.size() > 0 && pos < listTokens.size() ){
            Token tok = listTokens.get(pos);
            if(tok.getText().equals("*") || tok.getText().equals("/")){
                String aux = "aux_"+IsiVariable.getContAuxVar();
                IsiVariable v = new IsiVariable(aux, tipo);
                ArrayList<Token> lTk = new ArrayList<Token>();
                lTk.add(listTokens.get(pos-1));
                lTk.add(listTokens.get(pos));
                lTk.add(listTokens.get(pos+1));
                v.setListTk(lTk);
                tblVar.add(v);
                listVarAux.add(v);
                listTokens.remove(pos+1);
                listTokens.remove(pos);
                listTokens.remove(pos-1);
                pos--;
                Token tk = new Token(Token.TK_Variavel,aux);
                listTokens.add(pos, tk);
            }
            pos++;
        }
        pos = 0;
        while(listTokens.size() > 0 && pos < listTokens.size()){
            Token tok = listTokens.get(pos);
            if(tok.getText().equals("+") || tok.getText().equals("-")){
                String aux = "aux_"+IsiVariable.getContAuxVar();
                IsiVariable v = new IsiVariable(aux, tipo);
                ArrayList<Token> lTk = new ArrayList<Token>();
                lTk.add(listTokens.get(pos-1));
                lTk.add(listTokens.get(pos));
                lTk.add(listTokens.get(pos+1));
                v.setListTk(lTk);
                tblVar.add(v);
                listVarAux.add(v);
                listTokens.remove(pos+1);
                listTokens.remove(pos);
                listTokens.remove(pos-1);
                pos--;
                Token tk = new Token(Token.TK_Variavel,aux);
                listTokens.add(pos, tk);
            }
            pos++;
        }
        pos = 0;
        IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), tipo);
        ArrayList<Token> lTk = new ArrayList<Token>();
        lTk.add(listTokens.get(pos));
        v.setListTk(lTk);
        tblVar.add(v);
        listVarAux.add(v);
        listTokens.remove(pos);
    }

    // Verifica a condição do comando If
    public String condicao () {
        String tipo;
        nextTkn();
        switch(token.getType()){
            case Token.TK_Variavel:
                verificaVar(token.getText());
                if(tblVar.get(token.getText()).getType() == 0 || tblVar.get(token.getText()).getType() == 1){
                    tipo = "Numero";
                    ArrayList<Token> ltok = new ArrayList<Token>();
                    listTokens = ltok;
                    listTokens.add(token);
                    
                    conta(IsiVariable.doubleNum);
                    divExpr(IsiVariable.doubleNum);
                    
                    ArrayList<Token> ltkCond = new ArrayList<Token>();
                    String aux1 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    Token tk = new Token(Token.TK_Variavel, aux1);
                    ltkCond.add(tk);
                    
                    opRel();
                    ltkCond.add(token);

                    num();
                    
                    ltok = new ArrayList<Token>();
                    listTokens = ltok;
                    listTokens.add(token);

                    conta(IsiVariable.doubleNum);
                    divExpr(IsiVariable.doubleNum);

                    String aux2 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    tk = new Token(Token.TK_Variavel, aux2);
                    ltkCond.add(tk);
                    IsiVariable v = new IsiVariable("aux_"+(IsiVariable.getContAuxVar()), IsiVariable.doubleNum);
                    v.setListTk(ltkCond);
                    tblVar.add(v);
                    listVarAux.add(v);

                }else{
                    tipo = "Texto";
                    IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), IsiVariable.TEXT);
                    ArrayList<Token> lTk = new ArrayList<Token>();
                    lTk.add(token);
                    v.setListTk(lTk);
                    tblVar.add(v);
                    listVarAux.add(v);

                    ArrayList<Token> ltkCond = new ArrayList<Token>();
                    String aux1 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    Token tk = new Token(Token.TK_Variavel, aux1);
                    ltkCond.add(tk);

                    opRelText();

                    ltkCond.add(token);

                    texto();

                    v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), IsiVariable.TEXT);
                    lTk = new ArrayList<Token>();
                    lTk.add(token);
                    v.setListTk(lTk);
                    tblVar.add(v);
                    listVarAux.add(v);

                    String aux2 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    tk = new Token(Token.TK_Variavel, aux2);
                    ltkCond.add(tk);
                    v = new IsiVariable("aux_"+(IsiVariable.getContAuxVar()), IsiVariable.doubleNum);
                    v.setListTk(ltkCond);
                    tblVar.add(v);
                    listVarAux.add(v);
                    
                    nextTkn();
                }
                break;

            case Token.TK_Texto:
                tipo = "Texto";
                IsiVariable v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), IsiVariable.TEXT);
                ArrayList<Token> lTk = new ArrayList<Token>();
                lTk.add(token);
                v.setListTk(lTk);
                tblVar.add(v);
                listVarAux.add(v);

                ArrayList<Token> ltkCond = new ArrayList<Token>();
                String aux1 = "aux_"+(IsiVariable.getContAuxVar()-1);
                Token tk = new Token(Token.TK_Variavel, aux1);
                ltkCond.add(tk);

                opRelText();

                ltkCond.add(token);

                texto();

                v = new IsiVariable(("aux_"+IsiVariable.getContAuxVar()), IsiVariable.TEXT);
                lTk = new ArrayList<Token>();
                lTk.add(token);
                v.setListTk(lTk);
                tblVar.add(v);
                listVarAux.add(v);

                String aux2 = "aux_"+(IsiVariable.getContAuxVar()-1);
                tk = new Token(Token.TK_Variavel, aux2);
                ltkCond.add(tk);
                v = new IsiVariable("aux_"+(IsiVariable.getContAuxVar()), IsiVariable.doubleNum);
                v.setListTk(ltkCond);
                tblVar.add(v);
                listVarAux.add(v);
                
                nextTkn();
                break;

            default:
                if(token.getType() == Token.TK_IntNumero || token.getType() == Token.TK_DoubleNumero){
                    tipo = "Numero";
                    ArrayList<Token> ltok = new ArrayList<Token>();
                    listTokens = ltok;
                    listTokens.add(token);

                    conta(IsiVariable.doubleNum);
                    divExpr(IsiVariable.doubleNum);
                    
                    ltkCond = new ArrayList<Token>();
                    aux1 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    tk = new Token(Token.TK_Variavel, aux1);
                    ltkCond.add(tk);
                    
                    opRel();
 
                    ltkCond.add(token);

                    num();
                    
                    ltok = new ArrayList<Token>();
                    listTokens = ltok;

                    listTokens.add(token);

                    conta(IsiVariable.doubleNum);
                    divExpr(IsiVariable.doubleNum);

                    aux2 = "aux_"+(IsiVariable.getContAuxVar()-1);
                    tk = new Token(Token.TK_Variavel, aux2);
                    ltkCond.add(tk);
                    v = new IsiVariable("aux_"+(IsiVariable.getContAuxVar()), IsiVariable.doubleNum);
                    v.setListTk(ltkCond);
                    tblVar.add(v);
                    listVarAux.add(v);
                }else{
                    throw new IsiSyntaxException("Esperava-se uma variável, número ou texto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
                }
                break;
        }
        return tipo;
    }

    // Verifica se é um caso no comando escolha
    public void caso (){
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("caso")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimcaso'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é o fim do comando Se
    public void fimSe () {
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("fimse")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimse'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else{
            nextTkn();
            ponto();
        }
    }

    // Verifica se é o fim do comando enquanto
    public void fimEnquanto () {
        if(token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("fimenquanto")){
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimenquanto'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else {
            nextTkn();
            ponto();
        }
    }

    // Verifica se é o fim do caso no comando escolha
    public void fimCaso () {
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("fimcaso")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimcaso'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else {
            nextTkn();
            ponto();
        }
    }

    // Verifica se é o fim do comando escolha
    public void fimEscolha () {
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals("fimescolha")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'fimescolha'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else {
            nextTkn();
            ponto();
        }
    }

    // Verificar se a variável foi declarada
    public void verificaVar (String variavel) {
        if(!tblVar.exists(variavel)){
            throw new IsiSemanticException("Variavel "+variavel+" nao foi declarada");
        }
    }

    // Verifica se é uma variavel
    public void variavel () {
        nextTkn();
        if (token.getType() != Token.TK_Variavel) {
            throw new IsiSyntaxException("Esperava-se uma variável!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é uma variavel e se é um tipo de dado (Número ou Texto)
    public int tipoDado () {
        nextTkn();
        if (token.getType() != Token.TK_PResarvada || (!token.getText().toLowerCase().equals("pontof") && !token.getText().toLowerCase().equals("inteiro") && !token.getText().toLowerCase().equals("texto"))) {
            throw new IsiSyntaxException("Esperava-se um tipo 'inteiro', 'pontoF' ou 'texto'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
        if(token.getText().toLowerCase().equals("inteiro")){
            return 0;
        }else if (token.getText().toLowerCase().equals("pontof")){
            return 1;
        }else{
            return 2;
        }
    }
    
    // Verificar se é um número ou variavel do tipo numero
    public void num () {
        nextTkn();
        if (token.getType() != Token.TK_IntNumero && token.getType() != Token.TK_DoubleNumero && token.getType() != Token.TK_Variavel) {
            throw new IsiSyntaxException("Esperava-se um número ou variável do tipo número!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else if (token.getType() == Token.TK_Variavel){
            verificaVar(token.getText());
            if(tblVar.get(token.getText()).getType() == IsiVariable.TEXT){
                throw new IsiSyntaxException("Esperava-se um número ou variável do tipo número!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
            }
        }
    }

    // Verificar se é um número ou variavel do tipo numero
    public void DifVar (int tipo) {
        nextTkn();
        if (tipo == IsiVariable.TEXT && token.getType() != Token.TK_Texto) {
            throw new IsiSyntaxException("Esperava-se um texto! encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else if (tipo == IsiVariable.intNum && token.getType() != Token.TK_IntNumero) {
            throw new IsiSyntaxException("Esperava-se um número inteiro! encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um texto ou variavel do tipo texto
    public void texto () {
        nextTkn();
        if (token.getType() != Token.TK_Texto && token.getType() != Token.TK_Variavel) {
            throw new IsiSyntaxException("Esperava-se um texto ou variável do tipo texto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }else if (token.getType() == Token.TK_Variavel){
            verificaVar(token.getText());
            if(tblVar.get(token.getText()).getType() != IsiVariable.TEXT){
                throw new IsiSyntaxException("Esperava-se um texto ou variável do tipo texto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
            }
        }
    }

    // Verifica se é um operador de atribuição
    public void opAtrib () {
        nextTkn();
        if (token.getType() != Token.TK_OpAtrib) {
            throw new IsiSyntaxException("Esperava-se um Operador de Atribuição!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um operador aritmético
    public void opArit () {
        if (token.getType() != Token.TK_OpArit) {
            throw new IsiSyntaxException("Esperava-se um Operador Aritmético ou ponto!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um operador relacional
    public void opRel () {
        if (token.getType() != Token.TK_OpRel) {
            throw new IsiSyntaxException("Esperava-se um Operador relacional!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um operador relacional ==
    public void opRelText () {
        nextTkn();
        if (token.getType() != Token.TK_OpRel || (token.getType() == Token.TK_OpRel && !(token.getText().toLowerCase().equals("==") || token.getText().toLowerCase().equals("!=")))) {
            throw new IsiSyntaxException("Esperava-se um Operador relacional!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é o ID 'faca'
    public void faca () {
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals( "faca")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'faca'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é o ID 'entao'
    public void entao (){
        if (token.getType() != Token.TK_PResarvada || !token.getText().toLowerCase().equals( "entao")) {
            throw new IsiSyntaxException("Esperava-se a palavra reservada 'entao'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um '.'
    public void ponto (){
        if (token.getType() != Token.TK_Ponto) {
            throw new IsiSyntaxException("Esperava-se um ponto '.'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um ')'
    public void AP (){
        if (token.getType() != Token.TK_AbreParentes) {
            throw new IsiSyntaxException("Esperava-se um parênteses '('!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é um ')'
    public void FP (){
        if (token.getType() != Token.TK_FechaParentes) {
            throw new IsiSyntaxException("Esperava-se um parênteses ')'!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

    // Verifica se é ','
    public void virgula (){
        if (token.getType() != Token.TK_Virgula) {
            throw new IsiSyntaxException("Esperava-se uma vírgula ','!, encontrado "+Token.TK_TEXT[token.getType()]+" ("+token.getText()+") at Linha "+token.getLine()+" and Coluna "+token.getColumn());
        }
    }

}