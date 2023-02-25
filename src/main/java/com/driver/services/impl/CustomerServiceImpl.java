package com.driver.services.impl;

import com.driver.model.Driver;
import com.driver.model.TripBooking;
import com.driver.model.TripStatus;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer cust=customerRepository2.findById(customerId).get();
		customerRepository2.delete(cust);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query


		TripBooking newTrip=new TripBooking();
		Driver getdriver=null;

		List<Driver> getAllDrivers=driverRepository2.findAll();
		for(Driver driver:getAllDrivers){
			if(driver.getCab().getAvailable()==Boolean.TRUE){
				if((driver==null)||getdriver.getDriverId()>driver.getDriverId()){
					getdriver=driver;
				}
			}
		}
		if (getdriver==null){
			throw new Exception("No cab available!");
		}





		Customer customer=customerRepository2.findById(customerId).get();
		newTrip.setCustomer(customer);
		newTrip.setDriver(getdriver);
		getdriver.getCab().setAvailable(Boolean.FALSE);

		newTrip.setFromLocation(fromLocation);
		newTrip.setToLocation(toLocation);
		newTrip.setDistanceInKm(distanceInKm);
		int rate=getdriver.getCab().getPerKmRate();
		newTrip.setBill(10*distanceInKm);
		newTrip.setStatus(TripStatus.CONFIRMED);

		customer.getTripBookingList().add(newTrip);
		customerRepository2.save(customer);

		getdriver.getTripBookings().add(newTrip);

		driverRepository2.save(getdriver);


		return newTrip;


	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
//		tripBooking.setFromLocation(null);
//		tripBooking.setToLocation(null);
//		tripBooking.setDistanceInKm(0);
		tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);

		int bill=tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm();
		tripBooking.setBill(bill);
		tripBooking.getDriver().getCab().setAvailable(Boolean.TRUE);
		tripBookingRepository2.save(tripBooking);

	}
}
