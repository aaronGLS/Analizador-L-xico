package app;

import controller.MainController;

/**
 * Punto de entrada recomendado de la aplicación.
 * Delegamos toda la inicialización a {@link MainController}.
 */
public final class App {

	private App() { }

	public static void main(String[] args) {
		new MainController().start();
	}
}
