package ng.dominic.experiment.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

/**
 * Klein stukje geschreven om het een specifiek probleem op te lossen. Bewaren op github als voorbeeld voor later.
 *
 * Probleem: Er is een export gemaakt van .html bestanden en later is gebleken dat de img src attributen niet goed gevuld
 * werden. Dit kwam doordat in de oorspronkelijke setting de src werd gevoed vanuit Java. Nu de .html bestanden 'dumb'
 * losgetrokken zijn moeten de src referenties hardcoded ingevuld worden.
 *
 * @author <a href="mailto:abc@xyz.nl">Dominic Ng</a>
 * Created on 15-04-20
 */
public class TekstVervanger {

	private static final Path LOCATION = Paths.get("/home/dominic/Downloads/help");

	private static final int ROOT_DEPTH = LOCATION.getNameCount();

	private static final PathMatcher alleenHtml = FileSystems.getDefault().getPathMatcher("glob:**.html");

	public static void main(String[] args) {
		new TekstVervanger().run();
	}

	private void run() {
		findPaths(LOCATION).forEach(this::vervangen);
	}

	/**
	 * Gegeven een pad berekenen we het niveau waarin dit bestand zich bevindt en roepen de 'vervangen' methode aan
	 * om de src tag te overschrijven.
	 * @param pad
	 */
	private void vervangen(Path pad) {
		try(Stream<String> ingelezenLijntjes = Files.lines(pad)) {
			int niveaus = pad.getNameCount() - ROOT_DEPTH;
			List<String> tekst = ingelezenLijntjes
				.map(perLijn -> inputTekst(perLijn, vervangen(niveaus)))
				.collect(Collectors.toList());
			Files.write(pad, tekst);
		} catch(Exception e) {
			System.out.println("Het vervangen van tekstlijnen lukt niet");
		}
	}

	/**
	 * Gegeven een startlocatie lopen we alle subpaden af en geven de .html bestanden terug als een list.
	 * @param path
	 * @return
	 */
	private List<Path> findPaths(Path path) {
		try(Stream<Path> paden = Files.walk(path)) {
			return paden.filter(alleenHtml::matches).collect(Collectors.toList());
		} catch(Exception e) {
			System.out.println("Er is iets verkeerd gegaan bij het uitlezen van de paden -> we geven hier een lege list terug");
			return Collections.emptyList();
		}
	}

	/**
	 * Als de ingelezen str voorkomt in de map vervangen we de str met de value
	 * @param str
	 * @param map
	 * @return
	 */
	private static String inputTekst(String str, Map<String, String> map) {
		for(Entry<String, String> entry : map.entrySet()) {
			if(str.contains(entry.getKey())) {
				str = str.replace(entry.getKey(), entry.getValue());
			}
		}
		return str;
	}

	/**
	 * Hier definieren we welke tekst vervangen moet worden. Afhankelijk van de depth moeten er meer of minder niveaus
	 * omhoog gesprongen. In de .html bestanden heb ik steekproefsgewijs gevonden dat voor de src zowel enkele als
	 * dubbele quotes gebruikt worden.
	 *
	 * nb. vanaf Java11 is het ook mogelijk om String.repeat hiervoor te gebruiken, bijv:
	 * "src='" + String.join("", "../".repeat(depth)) + "General/Images/";
	 * @param depth
	 * @return
	 */
	private static Map<String, String> vervangen(int depth) {
		String enkeleQuotes = "src='" + String.join("", Collections.nCopies(depth, "../")) + "General/Images/";
		String dubbeleQuotes = "src=\"" + String.join("", Collections.nCopies(depth, "../")) + "General/Images/";
		Map<String, String> map = new HashMap<>();
		map.put("src='", enkeleQuotes);
		map.put("src=\"", dubbeleQuotes);
		return map;
	}
}
