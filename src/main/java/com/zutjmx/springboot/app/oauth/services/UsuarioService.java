package com.zutjmx.springboot.app.oauth.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.zutjmx.springboot.app.commons.usuarios.models.entity.Usuario;
import com.zutjmx.springboot.app.oauth.clients.UsuarioFeignClient;

import feign.FeignException;

@Service
public class UsuarioService implements IUsuarioService, UserDetailsService {
	
	private Logger logger = LoggerFactory.getLogger(UsuarioService.class);
	
	@Autowired
	private UsuarioFeignClient client;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			
			Usuario usuario = client.findByUsername(username);
			
			List<GrantedAuthority> authorities = usuario.getRoles()
					.stream()
					.map(role -> new SimpleGrantedAuthority(role.getNombre()))
					.peek(authority -> logger.info("Rol: ".concat(authority.getAuthority())))
					.collect(Collectors.toList());
			
			logger.info(":: Usuario autenticado "
					.concat(username)
					.concat(" ::"));
			
			return new User(usuario.getUsername(), 
					usuario.getPassword(), 
					usuario.getEnabled(), 
					true, 
					true, 
					true, 
					authorities);
			
		} catch (FeignException e) {
			logger.error("::Error en el login, no existe el usuario "
					.concat(username)
					.concat(" ::"));
			
			throw new UsernameNotFoundException("::Error en el login, no existe el usuario "
					.concat(username)
					.concat(" ::"));
		}
	}

	@Override
	public Usuario findByUsername(String username) {
		return client.findByUsername(username);
	}

	@Override
	public Usuario update(Usuario usuario, Long id) {
		return client.update(usuario, id);
	}

}
