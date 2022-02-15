package br.robertosamuelx.parkingcontrol.controllers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.robertosamuelx.parkingcontrol.dtos.ParkingSpotDTO;
import br.robertosamuelx.parkingcontrol.models.ParkingSpotModel;
import br.robertosamuelx.parkingcontrol.services.ParkingSpotService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

	final ParkingSpotService service;

	public ParkingSpotController(ParkingSpotService service) {
		this.service = service;
	}
	
	@PostMapping
	public ResponseEntity<Object> createParkingSpot(
			@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
		
		if(service.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar()))
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Conflict: License Plate Car is alread in use!");
		if(service.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber()))
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Conflict: Parking Spot is alread in use!");
		if(service.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock()))
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("Conflict: Parking Spot alread registered for this apartment/block!");
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(parkingSpotModel));
	}
	
	@GetMapping
	public ResponseEntity<Page<ParkingSpotModel>> listParkingSpots(
			@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
		return ResponseEntity.status(HttpStatus.OK).body(service.list(pageable));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Object> getParkingSpot(@PathVariable("id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModelOption = service.findById(id);
		if(!parkingSpotModelOption.isPresent())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		
		return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOption.get());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id){
		Optional<ParkingSpotModel> parkingSpotModel = service.findById(id);
		if(!parkingSpotModel.isPresent())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		
		service.delete(parkingSpotModel.get());
		return ResponseEntity.status(HttpStatus.OK).body("Parking Spot delete successfully");
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,
			@RequestBody @Valid ParkingSpotDTO parkingSpotDTO){
		Optional<ParkingSpotModel> parkingSpotModelOption = service.findById(id);
		if(!parkingSpotModelOption.isPresent())
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found");
		
		var parkingSpotModel = new ParkingSpotModel();
		BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
		parkingSpotModel.setId(parkingSpotModelOption.get().getId());
		parkingSpotModel.setRegistrationDate(parkingSpotModelOption.get().getRegistrationDate());
		return ResponseEntity.status(HttpStatus.OK).body(service.create(parkingSpotModel));
	}
}
