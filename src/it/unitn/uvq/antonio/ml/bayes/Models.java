package it.unitn.uvq.antonio.ml.bayes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Models implements Model {

	INTERNET("internet", "Internet", "nb-models/internet.tsv"),
	AVIATION("aviation", "Aviation", "nb-models/aviation.tsv"),
	COMMON("common", "Common", "nb-models/common.tsv"),
	INTERESTS("interests", "Interests", "nb-models/interests.tsv"),
	DISTILLED_SPIRITS("distilled_spirits", "Distilled Spirits", "nb-models/distilled_spirits.tsv"),
	CRICKET("cricket", "Cricket", "nb-models/cricket.tsv"),
	SPORTS("sports", "Sports", "nb-models/sports.tsv"),
	CELEBRITIES("celebrities", "Celebrities", "nb-models/celebrities.tsv"),
	BASKETBALL("basketball", "Basketball", "nb-models/basketball.tsv"),
	BUSINESS("business", "Business", "nb-models/business.tsv"),
	LOCATION("location", "Location", "nb-models/location.tsv"),
	TYPE("type", "Type", "nb-models/type.tsv"),
	EDUCATION("education", "Education", "nb-models/education.tsv"),
	COMPUTER("computer", "Computer", "nb-models/computer.tsv"),
	ENGINEERING("engineering", "Engineering", "nb-models/engineering.tsv"),
	SOCCER("soccer", "Soccer", "nb-models/soccer.tsv"),
	DATAWORLD("dataworld", "Dataworld", "nb-models/dataworld.tsv"),
	BICYCLES("bicycles", "Bicycles", "nb-models/bicycles.tsv"),
	ECONOMY("economy", "Economy", "nb-models/economy.tsv"),
	BOXING("boxing", "Boxing", "nb-models/boxing.tsv"),
	TRANSPORTATION("transportation", "Transportation", "nb-models/transportation.tsv"),
	QUOTATIONSBOOK("quotationsbook", "Quotationsbook", "nb-models/quotationsbook.tsv"),
	CONFERENCES("conferences", "Conferences", "nb-models/conferences.tsv"),
	DINING("dining", "Dining", "nb-models/dining.tsv"),
	BROADCAST("broadcast", "Broadcast", "nb-models/broadcast.tsv"),
	TIME("time", "Time", "nb-models/time.tsv"),
	TENNIS("tennis", "Tennis", "nb-models/tennis.tsv"),
	GEOGRAPHY("geography", "Geography", "nb-models/geography.tsv"),
	GAMES("games", "Games", "nb-models/games.tsv"),
	MEDICINE("medicine", "Medicine", "nb-models/medicine.tsv"),
	PROTECTED_SITES("protected_sites", "Protected Sites", "nb-models/protected_sites.tsv"),
	PROJECTS("projects", "Projects", "nb-models/projects.tsv"),
	METROPOLITAN_TRANSIT("metropolitan_transit", "Metropolitan Transit", "nb-models/metropolitan_transit.tsv"),
	MEDIA_COMMON("media_common", "Media Common", "nb-models/media_common.tsv"),
	MILITARY("military", "Military", "nb-models/military.tsv"),
	FINANCE("finance", "Finance", "nb-models/finance.tsv"),
	SPACEFLIGHT("spaceflight", "Spaceflight", "nb-models/spaceflight.tsv"),
	LIBRARY("library", "Library", "nb-models/library.tsv"),
	INFLUENCE("influence", "Influence", "nb-models/influence.tsv"),
	THEATER("theater", "Theater", "nb-models/theater.tsv"),
	ORGANIZATION("organization", "Organization", "nb-models/organization.tsv"),
	BASEBALL("baseball", "Baseball", "nb-models/baseball.tsv"),
	COMIC_STRIPS("comic_strips", "Comic Strips", "nb-models/comic_strips.tsv"),
	OLYMPICS("olympics", "Olympics", "nb-models/olympics.tsv"),
	VISUAL_ART("visual_art", "Visual Art", "nb-models/visual_art.tsv"),
	ARCHITECTURE("architecture", "Architecture", "nb-models/architecture.tsv"),
	ZOOS("zoos", "Zoos", "nb-models/zoos.tsv"),
	LANGUAGE("language", "Language", "nb-models/language.tsv"),
	MARTIAL_ARTS("martial_arts", "Martial Arts", "nb-models/martial_arts.tsv"),
	PERIODICALS("periodicals", "Periodicals", "nb-models/periodicals.tsv"),
	FASHION("fashion", "Fashion", "nb-models/fashion.tsv"),
	RELIGION("religion", "Religion", "nb-models/religion.tsv"),
	RADIO("radio", "Radio", "nb-models/radio.tsv"),
	ICE_HOCKEY("ice_hockey", "Ice Hockey", "nb-models/ice_hockey.tsv"),
	CVG("cvg", "Cvg", "nb-models/cvg.tsv"),
	EVENT("event", "Event", "nb-models/event.tsv"),
	GOVERNMENT("government", "Government", "nb-models/government.tsv"),
	BIOLOGY("biology", "Biology", "nb-models/biology.tsv"),
	METEOROLOGY("meteorology", "Meteorology", "nb-models/meteorology.tsv"),
	CHESS("chess", "Chess", "nb-models/chess.tsv"),
	LAW("law", "Law", "nb-models/law.tsv"),
	SYMBOLS("symbols", "Symbols", "nb-models/symbols.tsv"),
	CHEMISTRY("chemistry", "Chemistry", "nb-models/chemistry.tsv"),
	PEOPLE("people", "People", "nb-models/people.tsv"),
	BOOK("book", "Book", "nb-models/book.tsv"),
	TRAVEL("travel", "Travel", "nb-models/travel.tsv"),
	BOATS("boats", "Boats", "nb-models/boats.tsv"),
	OPERA("opera", "Opera", "nb-models/opera.tsv"),
	SKIING("skiing", "Skiing", "nb-models/skiing.tsv"),
	GEOLOGY("geology", "Geology", "nb-models/geology.tsv"),
	TV("tv", "Tv", "nb-models/tv.tsv"),
	MEASUREMENT_UNIT("measurement_unit", "Measurement Unit", "nb-models/measurement_unit.tsv"),
	EXHIBITIONS("exhibitions", "Exhibitions", "nb-models/exhibitions.tsv"),
	FOOD("food", "Food", "nb-models/food.tsv"),
	PIPELINE("pipeline", "Pipeline", "nb-models/pipeline.tsv"),
	MUSIC("music", "Music", "nb-models/music.tsv"),
	PHYSICS("physics", "Physics", "nb-models/physics.tsv"),
	AMUSEMENT_PARKS("amusement_parks", "Amusement Parks", "nb-models/amusement_parks.tsv"),
	FICTIONAL_UNIVERSE("fictional_universe", "Fictional Universe", "nb-models/fictional_universe.tsv"),
	COMIC_BOOKS("comic_books", "Comic Books", "nb-models/comic_books.tsv"),
	AWARD("award", "Award", "nb-models/award.tsv"),
	VENTURE_CAPITAL("venture_capital", "Venture Capital", "nb-models/venture_capital.tsv"),
	ASTRONOMY("astronomy", "Astronomy", "nb-models/astronomy.tsv"),
	RAIL("rail", "Rail", "nb-models/rail.tsv"),
	AUTOMOTIVE("automotive", "Automotive", "nb-models/automotive.tsv"),
	AMERICAN_FOOTBALL("american_football", "American Football", "nb-models/american_football.tsv"),
	WINE("wine", "Wine", "nb-models/wine.tsv"),
	ROYALTY("royalty", "Royalty", "nb-models/royalty.tsv"),
	DIGICAMS("digicams", "Digicams", "nb-models/digicams.tsv"),
	FILM("film", "Film", "nb-models/film.tsv");

	Models(String modelId, String modelName, String modelFile) {
		if (modelId == null) throw new NullPointerException("modelId: null");
		if (modelName == null) throw new NullPointerException("modelName: null");
		if (modelFile == null) throw new NullPointerException("modelFile: null");

		this.modelId = modelId;
		this.modelName = modelName;
		this.modelFile = modelFile;
		this.prob = readProb(modelFile);
		word2ProbMap = readProbs(modelFile);
	}

	@Override
	public String modelName() {
		return modelName;
	}

	@Override
	public double prob() {
		return prob;
	}

	@Override
	public double prob(String word) {
		if (word2ProbMap.containsKey(word)) {
			return word2ProbMap.get(word);
		}
		return .0;
	}

	private double readProb(String modelFile) {
		assert modelFile != null;

		BufferedReader in = null;
		double prob = .0;
		try {
			in = new BufferedReader(
				new FileReader(modelFile));
			String line = in.readLine();
			if (line != null) { prob = Double.parseDouble(line); }
		} catch (FileNotFoundException e) { 
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			}catch (IOException e) { }
		}
		return prob;
}

	private Map<String, Double> readProbs(String modelFile) { 
		assert modelFile != null;

		Map<String, Double> probs = new HashMap<>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(
				new FileReader(modelFile));
			String line = in.readLine();
			if (line != null) {
				while ((line = in.readLine()) != null) {
					String[] values = line.split(SEPARATOR);
					String word = values[0];
					double prob = Double.parseDouble(values[1]);
					probs.put(word, prob);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
		return Collections.unmodifiableMap(probs);
	}

	private final static String SEPARATOR = "\t";

	private final String modelId;

	private final String modelName;

	private final String modelFile;

	private final double prob;

	private final Map<String, Double> word2ProbMap;

}
