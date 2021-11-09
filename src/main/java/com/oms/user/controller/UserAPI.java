package com.oms.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.oms.user.dto.BuyerDTO;
import com.oms.user.dto.CartDTO;
import com.oms.user.dto.ProductDTO;
import com.oms.user.dto.SellerDTO;
import com.oms.user.exception.UserMsException;
import com.oms.user.service.UserService;

@RestController
@CrossOrigin
public class UserAPI {
	
	@Autowired
	private UserService userServiceNew;
	
//	@Autowired
//	DiscoveryClient client;
	
	@Autowired
	Environment environment;
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@Value("${product.uri}")
	String prodUri;
	private static final String TOPIC = "kafka-user2";

	@GetMapping("userMS/publish/{message}") public String post(@PathVariable("message")
	  final String message) {
	  kafkaTemplate.send(TOPIC,message);
	  
	  System.out.println("published");
	  
	  return "Published successfully"; }
	
	
	@PostMapping(value = "/userMS/buyer/register")
	public ResponseEntity<String> registerBuyer(@RequestBody BuyerDTO buyerDto){
		
		try {
		String s ="Buyer registered successfully with buyer Id : " + userServiceNew.buyerRegistration(buyerDto);
		return new ResponseEntity<>(s,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			String s = environment.getProperty(e.getMessage());
			return new ResponseEntity<>(s,HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@PostMapping(value = "/userMS/seller/register")
	public ResponseEntity<String> registerSeller(@RequestBody SellerDTO sellerDto){
		
		try {
		String s ="Seller registered successfully with seller Id : "+ userServiceNew.sellerRegistration(sellerDto);
		return new ResponseEntity<>(s,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			return new ResponseEntity<>(environment.getProperty(e.getMessage()),HttpStatus.EXPECTATION_FAILED);
		}

	}
	
	@PostMapping(value = "/userMS/buyer/login/{email}/{password}")
	public ResponseEntity<String> loginBuyer(@PathVariable String email, @PathVariable String password)
	{
		try {
			String msg = userServiceNew.buyerLogin(email, password);
			return new ResponseEntity<>(msg,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			return new ResponseEntity<>(e.getMessage(),HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping(value = "/userMS/seller/login/{email}/{password}")
	public ResponseEntity<String> loginSeller(@PathVariable String email, @PathVariable String password)
	{
		try {
			String msg = userServiceNew.sellerLogin(email, password);
			return new ResponseEntity<String>(msg,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.NOT_FOUND);
		}
	}
	@PutMapping(value = "/buyer/deactivate/{id}")
	public ResponseEntity<String> deactivateBuyerAccount(@PathVariable String id){
		try {
			String msg = userServiceNew.deactivateBuyer(id);
			return new ResponseEntity<String>(msg, HttpStatus.OK);
		}
		catch(Exception e)
		{
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.NOT_FOUND);
		}
	}
	
	@PutMapping(value = "/seller/deactivate/{id}")
	public ResponseEntity<String> deactivateSellerAccount(@PathVariable String id){
		try {
			String msg = userServiceNew.deactivateSeller(id);
			return new ResponseEntity<String>(msg, HttpStatus.OK);
		}
		catch(Exception e)
		{
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.NOT_FOUND);
		}
	}
	
	
	
	@DeleteMapping(value = "/userMS/buyer/deleteAccount/{id}")
	public ResponseEntity<String> deleteBuyerAccount(@PathVariable String id){
		
		String msg = userServiceNew.deleteBuyer(id);
		
		return new ResponseEntity<>(msg,HttpStatus.OK);
		
		
	}
	
	@DeleteMapping(value = "/userMS/seller/deleteAccount/{id}")
	public ResponseEntity<String> deleteSellerAccount(@PathVariable String id){
		
		String msg = userServiceNew.deleteSeller(id);
		
		return new ResponseEntity<>(msg,HttpStatus.OK);
	}
	
	@PostMapping(value = "/userMS/buyer/wishlist/add/{buyerId}/{prodId}")
	public ResponseEntity<String> addProductToWishlist(@PathVariable String buyerId, @PathVariable String prodId) throws UserMsException
	{
		try {
//		List<ServiceInstance> instances=client.getInstances("PRODUCTMS");
//		ServiceInstance instance=instances.get(0);
//		URI productUri = instance.getUri();
		
		ProductDTO product = new RestTemplate().getForObject(prodUri+"/prodMS/getById/"+prodId, ProductDTO.class);
		
		String msg = userServiceNew.wishlistService(product.getProdId(), buyerId);
		
		return new ResponseEntity<>(msg,HttpStatus.ACCEPTED);
		}
		catch(Exception e)
		{
			System.out.println(e);
			String newMsg = "There was some error";
			if(e.getMessage().equals("404 null"))
			{
				newMsg = "There are no PRODUCTS for the given product ID";
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,newMsg,e);
		}
	}
	@PostMapping(value = "/userMS/buyer/wishlist/remove/{buyerId}/{prodId}")
	public ResponseEntity<String> removeFromWishlist(@PathVariable String buyerId,@PathVariable String prodId) throws UserMsException
	{
		
		//ProductDTO product = new RestTemplate().getForObject("http://localhost:8100/prodMs/getByName/"+prodName, ProductDTO.class);
		try {
		String msg = userServiceNew.removeFromWishlist(buyerId, prodId);
		
		return new ResponseEntity<>(msg,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			String msg = e.getMessage();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg, e);
			
		}
	}
	
	@PostMapping(value = "/userMS/buyer/cart/add/{buyerId}/{prodId}/{quantity}")
	public ResponseEntity<String> addProductToCart(@PathVariable String buyerId, @PathVariable String prodId, @PathVariable Integer quantity) throws UserMsException
	{
		try {
//		List<ServiceInstance> instances=client.getInstances("PRODUCTMS");
//		ServiceInstance instance=instances.get(0);
//		URI productUri = instance.getUri();
//		System.out.println(productUri);
		
		 
		ProductDTO product = new RestTemplate().getForObject(prodUri+"/prodMS/getById/"+prodId, ProductDTO.class);
		System.out.println(product);
		System.out.println(product instanceof ProductDTO);
		String msg = userServiceNew.cartService(product.getProdId(), buyerId, quantity);
		String msg1="";
		msg1=msg1.concat(buyerId+" ");
		msg1=msg1.concat(prodId+" ");
		msg1=msg1.concat(String.valueOf(quantity)+" ");
		kafkaTemplate.send(TOPIC,msg1);
		
		return new ResponseEntity<>(msg,HttpStatus.ACCEPTED);
		}
		catch(Exception e)
		{
			String newMsg = "There was some error";
			if(e.getMessage().equals("404 null"))
			{
				newMsg = "There are no PRODUCTS for the given product ID";
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,newMsg,e);
		}
	}
	
	
	@GetMapping(value = "/userMS/buyer/cart/get/{buyerId}")
	public ResponseEntity<List<CartDTO>> getProductListFromCart(@PathVariable String buyerId) throws UserMsException
	{
		
		//ProductDTO product = new RestTemplate().getForObject("http://localhost:8100/prodMs/getByName/"+prodName, ProductDTO.class);
		try {
		List<CartDTO> list = userServiceNew.getCartProducts(buyerId);
		
		return new ResponseEntity<>(list,HttpStatus.ACCEPTED);
		}
		catch(UserMsException e)
		{
			System.out.println(e.getMessage());
			String msg = e.getMessage();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg, e);
			
		}
	}
	
	@PostMapping(value = "/userMS/buyer/cart/remove/{buyerId}/{prodId}")
	public ResponseEntity<String> removeFromCart(@PathVariable String buyerId,@PathVariable String prodId) throws UserMsException
	{
		
		//ProductDTO product = new RestTemplate().getForObject("http://localhost:8100/prodMs/getByName/"+prodName, ProductDTO.class);
		try {
		String msg = userServiceNew.removeFromCart(buyerId, prodId);
		
		return new ResponseEntity<>(msg,HttpStatus.OK);
		}
		catch(UserMsException e)
		{
			String msg = e.getMessage();
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, msg, e);
			
		}
	}
	@PostMapping(value = "/userMS/buyer/move/FromWishlist/Tocart/{buyerId}/{prodId}/{quantity}")
	public ResponseEntity<String> moveFromWihlistToCart(@PathVariable String buyerId, @PathVariable String prodId, @PathVariable Integer quantity) throws UserMsException
	{
		try {
		ProductDTO product = new RestTemplate().getForObject(prodUri+"/prodMS/getById/"+prodId, ProductDTO.class);
		System.out.println(product);
		System.out.println(product instanceof ProductDTO);
		String msg = userServiceNew.moveFromWihlistToCart(product.getProdId(), buyerId, quantity);
		String msg1="";
		msg1=msg1.concat(buyerId+" ");
		msg1=msg1.concat(prodId+" ");
		msg1=msg1.concat(String.valueOf(quantity)+" ");
		kafkaTemplate.send(TOPIC,msg1);
		
		return new ResponseEntity<>(msg,HttpStatus.ACCEPTED);
		}
		catch(Exception e)
		{
			String newMsg = "There was some error";
			if(e.getMessage().equals("404 null"))
			{
				newMsg = "There are no PRODUCTS for the given product ID";
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,newMsg,e);
		}
	}
	
	@PutMapping(value = "/userMS/updateRewardPoints/{buyerId}/{rewPoints}")
	public ResponseEntity<String> updateRewardPoints(@PathVariable String buyerId, @PathVariable Integer rewPoints)
	{
		try {
			String msg = userServiceNew.updateRewardPoint(buyerId, rewPoints);
			return new ResponseEntity<>(msg,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage(),e);
		}
	}
	@PutMapping(value = "/updateRewardPoints/deduct/{buyerId}/{points}")
	public ResponseEntity<String> updateRewardPointsDeduct(@PathVariable String buyerId, @PathVariable Integer points) throws UserMsException
	{
		try {
			String message=userServiceNew.updateRewardPointsReduce(buyerId, points);
			return new ResponseEntity<String>(message, HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}
	@GetMapping(value = "/buyer/getBuyerMode/{buyerId}")
	public ResponseEntity<String> getUserMode(@PathVariable String buyerId) throws UserMsException{
		try {
			String message=userServiceNew.getBuyerMode(buyerId);
			return new ResponseEntity<String>(message, HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}
	@GetMapping(value = "/userMS/optForPrivilegeMode/{buyerId}")
	public ResponseEntity<String> optForPrivilegeMode(@PathVariable String buyerId)
	{
		try {
			String msg = userServiceNew.optForPrivilegeMode(buyerId);
			return new ResponseEntity<>(msg,HttpStatus.OK);
		}
		catch(Exception e)
		{
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,e.getMessage(),e);
		}
	}
	@PostMapping(value = "/buyer/subscribeProduct/{buyerId}/{prodId}/{quantity}")
	public ResponseEntity<String> subscribeToProduct(@PathVariable String buyerId, @PathVariable String prodId, @PathVariable Integer quantity) throws UserMsException
	{
		String s=userServiceNew.subscribeProduct(buyerId);
		if(s.equals("False"))
		{
			return new ResponseEntity<String>("You cannot subscribe to a product as you do not have privilege mode. Please upgrade to privilege mode", HttpStatus.UNAUTHORIZED);
		}
		else
		{
			String message=new RestTemplate().postForObject(prodUri+"/subscribeProduct/"+buyerId+"/"+prodId+"/"+quantity, null, String.class);
			return new ResponseEntity<String>(message, HttpStatus.OK);
		}
	}
		
	}