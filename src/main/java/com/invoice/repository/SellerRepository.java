package com.invoice.repository;


import com.invoice.model.Seller;
import java.time.LocalDate;

public interface SellerRepository {

    public Seller getSellerByVatAndSerial();

    public int checkIsSellerExists(String vatNumber,String serialNumber);

    public void createSeller(Seller seller) ;

    public void update(Seller seller) ;

    public int checkIsSellerInfoExists(String vatNumber,String serialNumber);

    public LocalDate getExpiryDate();

    public void createSellerInfo(Seller seller);

    public void updateSellerInfo(Seller seller) ;
}
