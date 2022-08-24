# Compiladores-UFABC-2Q22

**Nome:** Gabriel de Sousa Dias

**RA:** 11201720272

## Checklist:

**Itens Obrigatórios contidos no projeto:**

- Possuí 2 tipos de dados. (Número Decimal e String);
- Possuí a instrução de decisão (if/else);
- Possuí a estrutura de repetição (while);	
- Verifica Atribuições com compatibilidade de tipos (semântica); 	
- Possuí operações de Entrada e Saída;
- Aceita números decimais; 	
- Verifica declaração de variávies (não usar variáveis que não foram declaradas);	
- Verifica se há variáveis declaradas e não-utilizadas (warning);	e
- Geração de código na linguagem Java.

**Elementos adicionais:**

- Possuí a instrução Switch/Case (escolha/caso);
- Possuí o tipo de dado Número Inteiro; e
- Geração de código na linguagem C.

## Link Vídeo Youtube:

<a href="https://youtu.be/TJFqEFBDpe0" target="_blank" rel="noopener">Link</a>

## Gramática

Prog	->	**programa** Declara Bloco **fimprog** .

Declara	->	**declare** Variavel (, Variavel)*.

Bloco	->	(Cmd)+

Cmd	->	CmdLeitura | CmdEscrita | CmdAtribuição | CmdSe | CmdEnquanto | CmdEscolha

CmdLeitura	->	**leia** AParenteses Variavel FParenteses.

CmdEscrita	->	**escreva** AParenteses (Variavel | Expr | Texto) FParenteses.

CmdSe	->	**se** AParenteses Expr OpRel Expr FParenteses **entao** Cmd+ (**senao** Cmd+ )? **fimse** .

CmdAtribuição	->	Variavel := Expr .

CmdEnquanto	->	**enquanto** AParenteses Expr OpRel Expr FParenteses **faca** (Cmd)+ **fimenquanto**.

CmdEscolha	->	**escolha** AParenteses Variavel FParenteses **faca** (**caso** Fator **faca** (Cmd)+ **fimcaso**.)+ (**semcaso faca** (Cmd)+ **fimcaso**.)? **fimescolha**.

OpRel	->	!= | == | < | > | <= | >= 

Expr	->	Expr OpArit Fator | Fator .

Fator	->	NumInteiro | NumPontoF | Texto |  Variavel

OpArit	->	+ | - | * | /

Texto	->	" (0..9 | a..z | A..Z  | OpRel | OpArit | ' ' )+ "

NumInteiro	->	(0..9)+

NumPontoF	->	NumInteiro , NumInteiro

Variavel	->	(a..z | A..Z) (a..z | A..Z | 0..9)*

AParenteses	->	(

FParenteses	->	)

## Analisador Léxico

<img src = "https://github.com/sdias22/Compiladores-UFABC-2Q22/blob/main/Analisador%20L%C3%A9xico.png">
