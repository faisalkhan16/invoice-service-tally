package com.invoice;

import com.invoice.repository.SellerRepositoryImplMySQL;
import com.invoice.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class App implements CommandLineRunner {

	@Value("${STANDARD_TAX_RATE}")
	private String STANDARD_TAX_RATE;

	private final SellerRepositoryImplMySQL sellerRepository;

	public static void main(String[] args) {
		SpringApplication.run(App.class);
	}

	@Override
	public void run(String... args) {

		Constants.STANDARD_TAX_RATE = Integer.parseInt(STANDARD_TAX_RATE);

		if (null != sellerRepository) {
			Constants.SELLER_EXPIRE_DATE = sellerRepository.getExpiryDate();
		}
	}

}