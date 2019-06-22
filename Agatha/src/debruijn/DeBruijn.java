package debruijn;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class DeBruijn {
	private Model modelo;

	private int tamAlfabeto; // k
	private int tamPalavra; // n
	private int tamSequenciaFinal;

	private IntVar[] numDec; // sequencias em decimal
	private IntVar[][] numBin; // decimais em binario
	private IntVar[] finalBin; // sequencia De Bruijn 

	public DeBruijn(int tamAlfabeto, int tamPalavra) {
		modelo = new Model();
		this.tamAlfabeto = tamAlfabeto;
		this.tamPalavra = tamPalavra;
		this.tamSequenciaFinal = this.pow(tamAlfabeto, tamPalavra);
		this.iniciaVar();
		this.iniciaRestricoes();
	}

	private void iniciaVar() {		
		numDec = modelo.intVarArray("numDec", tamSequenciaFinal, 0, tamSequenciaFinal - 1, true);
		numBin = new IntVar[tamSequenciaFinal][tamPalavra];
		finalBin = new IntVar[tamSequenciaFinal];	
	}

	private void iniciaRestricoes() {
		//converte decimal para base numerica igual ao tamanho do alfabeto...
		int[] pesos = new int[tamPalavra];
		int w = 1;
		for (int i = 0; i < tamPalavra; i++) {
			pesos[tamPalavra - i - 1] = w;
			w *= tamAlfabeto;
		}
		for (int i = 0; i < tamSequenciaFinal; i++) {
			numBin[i] = modelo.intVarArray("numBin - " + i, tamPalavra, 0, tamAlfabeto - 1, true);
			modelo.scalar(numBin[i], pesos, "=", numDec[i]).post();
		}
		// elemento i deve comecar com o fim do elemento i-1
		for (int i = 1; i < tamSequenciaFinal; i++) {
			for (int j = 1; j < tamPalavra; j++) {
				modelo.arithm(numBin[i - 1][j], "=", numBin[i][j - 1]).post();
			}
		}
		// ultimo elemento é conectado com o primeiro
		for (int j = 1; j < tamPalavra; j++) {
			modelo.arithm(numBin[tamSequenciaFinal - 1][j], "=", numBin[0][j - 1]).post();
		}
		// sequencia final = primeiro elemento de cada linha da matriz final
		for (int i = 0; i < tamSequenciaFinal; i++) {
			finalBin[i] = modelo.intVar("finalBin - " + i, 0, tamAlfabeto - 1, true);
			modelo.arithm(finalBin[i], "=", numBin[i][0]).post();
		}
		// todos os elementos em decimal devem ser diferentes
		 modelo.allDifferent(numDec).post();
		 // o menor valor em decimal tem que ser o primeiro elemento
		 modelo.min(numDec[0], numDec).post();
	}
	
	public void resolver() {
		Solver solver = modelo.getSolver();		
		solver.solve();
		
		this.printEnunciado();
		
		System.out.println("TAMANHO ALFABETO (k): "+ tamAlfabeto);
		System.out.println("TAMANHO PALAVRAS (n): "+ tamPalavra);
		System.out.println("TAMANHO SEQUENCIA FINAL (tamanho alfabeto ^ tamanho palavra): "+ tamSequenciaFinal);
		
		System.out.println("*********************************************************");
		
        System.out.print("SEQUENCIA ENCONTRADA (S): ");
        for(int i = 0; i < tamSequenciaFinal; i++) {
            System.out.print(finalBin[i].getValue() + " ");
        }
        System.out.println();
        System.out.println("*********************************************************");
        System.out.print("Alfabeto: ");        
        for(int i = 0; i < tamAlfabeto; i++) {
            System.out.print(i+" ");
        }
        System.out.println();
        System.out.println("Sub sequencias de tamanho 'n':");        
        for(int i = 0; i < tamSequenciaFinal; i++) {
            for(int j = 0; j < tamPalavra; j++) {
                System.out.print(numBin[i][j].getValue());
            }
//            System.out.println(" : " + numDec[i].getValue());
            System.out.println();
        }
        System.out.println("*********************************************************");
        solver.printStatistics();
	}
	private void printEnunciado() {
		System.out.println("*********************************************************");
		System.out.println("\t"+"Dado um inteiro n e um conjunto de caracteres A de tamnho k, ");
		System.out.println("\t"+"encontre uma sequencia S que contenha todas as sequencias");
		System.out.println("\t"+"possiveis de S com tamanho n.");
		
		System.out.println("*********************************************************");
	}

	private int pow(int x, int y) {
		int z = x;
		for (int i = 1; i < y; i++)
			z *= x;
		return z;
	}

}
