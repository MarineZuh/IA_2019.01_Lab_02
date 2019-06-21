package problema;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

public class QuemMatouAgatha {
	
	private final int QNT_PERSONAGENS = 3;
	private final int AGATHA = 0;
	private final int ERIC = 1;
	private final int CHARLES = 2;

	private Model modelo;

	private IntVar assassino;
	private IntVar[][] odeia;
	private IntVar[][] maisRico;
	
	public QuemMatouAgatha() {
		modelo = new Model();
		iniciaVariaveis();
		iniciaRestricoes();
	}
	
	private void iniciaVariaveis() {
		assassino = modelo.intVar("assassino", 0, QNT_PERSONAGENS - 1);
		odeia = new IntVar[QNT_PERSONAGENS][QNT_PERSONAGENS];
		maisRico = new IntVar[QNT_PERSONAGENS][QNT_PERSONAGENS];
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			for (int j = 0; j < QNT_PERSONAGENS; j++) {
				odeia[i][j] = modelo.intVar( getPersonagem(i) + " odeia " + getPersonagem(j), 0, 1);
				maisRico[i][j] = modelo.intVar(getPersonagem(i) + " � mais rico que " + getPersonagem(j), 0, 1);
			}
		}
	}
	
	private void iniciaRestricoes() {
		postRestricaoAssassino();
		postRestricaoMaisRico();
		postRestricaoOdeia();		
	}
	
	private void postRestricaoAssassino() {
		// assassino odeia a vitima 
		// assassino n�o � mais rico que a vitima
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			modelo.ifThen(
				modelo.arithm(assassino, "=", i),
				modelo.arithm(odeia[i][AGATHA], "=", 1)
			);
			modelo.ifThen(
				modelo.arithm(assassino, "=", i),
				modelo.arithm(maisRico[i][AGATHA], "=", 0)
			);
		}
	}
	
	private void postRestricaoOdeia() {
		// Charles n�o odeia quem Agatha odeia
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			modelo.ifThen(
				modelo.arithm(odeia[AGATHA][i], "=", 1),
				modelo.arithm(odeia[CHARLES][i], "=", 0)
			);
		}
		// Agatha odeia a todos, menos Eric
		modelo.arithm(odeia[AGATHA][CHARLES], "=", 1).post();
		modelo.arithm(odeia[AGATHA][AGATHA], "=", 1).post();
		modelo.arithm(odeia[AGATHA][ERIC], "=", 0).post();
		// Eric odeia todos que n�o s�o mais ricos que Agatha
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			modelo.ifThen(
				modelo.arithm(maisRico[i][AGATHA], "=", 0), 
				modelo.arithm(odeia[ERIC][i], "=", 1)
			);
		}		
		// Ninguem odeia todo mundo
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			IntVar valores[] = modelo.intVarArray(getPersonagem(i)+" rela��es de odeia ", QNT_PERSONAGENS, 0, 1);
			for (int j = 0; j < QNT_PERSONAGENS; j++) {
				valores[j] = odeia[i][j];				
			}			
			modelo.sum(valores, "<=", 2).post();	
		}		
	}
	
	private void postRestricaoMaisRico() {
		// ninguem � mais rico que ele mesmo
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			modelo.arithm(maisRico[i][i], "=", 0).post();
		}
		// se i � mais rico que j, j n�o � mais rico que i
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			for (int j = 0; j < QNT_PERSONAGENS; j++) {
				if (i != j) {
					modelo.ifOnlyIf(
						modelo.arithm(maisRico[i][j], "=", 1), 
						modelo.arithm(maisRico[j][i], "=", 0)
					);
				}
			}
		}
	}
	
	private String getPersonagem(int index) {
		switch(index) {
			case 0: return "Agatha";
			case 1: return "Eric";
			case 2: return "Charles";
		}
		return null;
	}
	public void resolver() {
		Solver solver = modelo.getSolver();		
		solver.solve();
		String assasinoEncontrado = getPersonagem(assassino.getValue());
		
		printEnunciado();
		System.out.println("VALORES OBTIDOS:");
		System.out.println();
		printOdeia();
		System.out.println();
		printMaisRico();
		System.out.println();
		System.out.println("-> Assassino = " + assasinoEncontrado);
		System.out.println("*********************************************************");
		solver.printStatistics();
	}
	
	private void printEnunciado() {
		System.out.println("*********************************************************");
		System.out.println("\t"+"Alguem na mans�o matou tia Agatha.");
		System.out.println("\t"+"Agatha, Eric e Charles s�o os �nicos morando na mans�o.");
		System.out.println("\t"+"O assassino odeia a vitima e n�o � mais rico que ela.");
		System.out.println("\t"+"Charles n�o odeia quem Agatha odeia.");
		System.out.println("\t"+"Agatha odeia a todos, menos Eric.");
		System.out.println("\t"+"Eric odeia todos que n�o s�o mais ricos que Agatha.");
		System.out.println("\t"+"Ninguem odeia todo mundo.");
		System.out.println("\n\t"+"Quem � o assassino?");
		System.out.println("*********************************************************");
	}
	
	private void printOdeia() {
		System.out.println("-> Quem odeia quem:");
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			for (int j = 0; j < QNT_PERSONAGENS; j++) {
				if(odeia[i][j].getValue() == 1) {
					System.out.println("\t- "+odeia[i][j].getName());
				}
			}	
		}
	}
	
	private void printMaisRico() {
		System.out.println("-> Quem � mais rico que quem:");
		for (int i = 0; i < QNT_PERSONAGENS; i++) {
			for (int j = 0; j < QNT_PERSONAGENS; j++) {
				if(maisRico[i][j].getValue() == 1) {
					System.out.println("\t- "+maisRico[i][j].getName());
				}
			}	
		}
	}
}
