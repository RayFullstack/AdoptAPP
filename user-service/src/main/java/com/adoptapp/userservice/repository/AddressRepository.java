package com.adoptapp.userservice.repository;

import com.adoptapp.userservice.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<UserAddress,Long> {

    boolean existsByPostalCode(String postalcode); //eliminar

    List<UserAddress> findByCountryIgnoreCase(String country);

    List<UserAddress> findByCityIgnoreCase(String city);

    List<UserAddress> findByPostalCodeIgnoreCase(String postalCode);
}
