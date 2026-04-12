package edu.cit.pangilinan.stillness.service;

import com.stillness.adapter.QuoteDTO;
import com.stillness.adapter.ZenQuotesAdapter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuoteService {

	private static final List<QuoteDTO> FALLBACK_QUOTES = List.of(
			new QuoteDTO("Peace comes from within. Do not seek it without.", "Buddha", "local"),
			new QuoteDTO("The quieter you become, the more you can hear.", "Ram Dass", "local"),
			new QuoteDTO("Stillness is where creativity and solutions are found.", "StillNess", "local"),
			new QuoteDTO("Inhale the future, exhale the past.", "Unknown", "local")
	);

	private final ZenQuotesAdapter zenQuotesAdapter;

	public QuoteService(ZenQuotesAdapter zenQuotesAdapter) {
		this.zenQuotesAdapter = zenQuotesAdapter;
	}

	public QuoteDTO getRandomQuote() {
		try {
			return zenQuotesAdapter.fetchQuote();
		} catch (Exception ex) {
			return FALLBACK_QUOTES.get(ThreadLocalRandom.current().nextInt(FALLBACK_QUOTES.size()));
		}
	}
}
