package edu.cit.pangilinan.stillness.quote;

import edu.cit.pangilinan.stillness.quote.adapter.QuoteDTO;
import edu.cit.pangilinan.stillness.shared.config.ApiResponse;
import edu.cit.pangilinan.stillness.quote.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/quotes")
public class QuoteController {

	private final QuoteService quoteService;

	public QuoteController(QuoteService quoteService) {
		this.quoteService = quoteService;
	}

	@GetMapping("/random")
	public ResponseEntity<ApiResponse<QuoteDTO>> getRandomQuote() {
		QuoteDTO quote = quoteService.getRandomQuote();
		return ResponseEntity.ok(ApiResponse.<QuoteDTO>builder()
				.success(true)
				.data(quote)
				.timestamp(LocalDateTime.now().toString())
				.build());
	}
}
