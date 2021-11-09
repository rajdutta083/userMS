package com.oms.user.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.oms.user.entity.Cart;
import com.oms.user.entity.Wishlist;
import com.oms.user.utility.CustomPK;


public interface WishlistRepository extends CrudRepository<Wishlist, CustomPK> {
	//public List<Wishlist> findByCustomPKBuyerId(String id); 
	public Wishlist findByCustomIdBuyerIdAndCustomIdProdId(String buyerId,String prodId);
	public void deleteByCustomIdBuyerIdAndCustomIdProdId(String buyerId,String prodId);
}