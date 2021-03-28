package com.predic8.stock.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.predic8.stock.error.NotFoundException;
import com.predic8.stock.model.Stock;
import com.predic8.stock.event.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequestMapping("/stocks")
@RestController
public class StockRestController {
	private final Map<String, Stock> stocks;

	private ObjectMapper mapper;

	private KafkaTemplate<String, Operation> kafka;

	public StockRestController(Map<String, Stock> articles, ObjectMapper mapper, KafkaTemplate<String, Operation> kafka) {
		this.stocks = articles;
		this.mapper = mapper;
		this.kafka = kafka;
	}

	@GetMapping
	public Collection<Stock> index() {
		return stocks.values();
	}

	@GetMapping("/count")
	public long count() {
		return stocks.size();
	}

	@GetMapping("/{uuid}")
	public Stock getStock(@PathVariable String uuid) {
		return stocks.get(uuid);
	}


	@PutMapping("/{uuid}")
	public ResponseEntity<Void> setQuantity(@PathVariable String uuid, @RequestBody Stock stock) {
		Stock oldStock = getStock(uuid);
		if( oldStock != null) {
			stock.setUuid(oldStock.getUuid());
			Operation op = new Operation("stock", "upsert", mapper.valueToTree(stock));
			kafka.send("shop",op);
			return ResponseEntity.accepted().build();
		}
		return ResponseEntity.notFound().build();
	}
}