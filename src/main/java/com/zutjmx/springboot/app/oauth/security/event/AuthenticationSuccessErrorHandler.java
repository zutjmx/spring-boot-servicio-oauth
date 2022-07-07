package com.zutjmx.springboot.app.oauth.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.zutjmx.springboot.app.commons.usuarios.models.entity.Usuario;
import com.zutjmx.springboot.app.oauth.services.IUsuarioService;

import feign.FeignException;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {
	
	private Logger logger = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

	@Autowired
	private IUsuarioService usuarioService;
	
	@Override
	public void publishAuthenticationSuccess(Authentication authentication) {
		
		if(/*authentication.getName().equalsIgnoreCase("frontendapp")*/
		   authentication.getDetails() instanceof WebAuthenticationDetails) {
			return;
		}
		
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String mensaje = ":: Login exitoso: ".concat(userDetails.getUsername()).concat(" ::");
		System.out.println(mensaje);
		logger.info(mensaje);
		
		Usuario usuario = usuarioService.findByUsername(authentication.getName());
		if(usuario.getIntentos() != null && usuario.getIntentos() > 0) {
			usuario.setIntentos(0);
			usuarioService.update(usuario, usuario.getId());
		}
		
	}

	@Override
	public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		String mensaje = ":: Login no exitoso: ".concat(exception.getMessage()).concat(" ::");
		System.out.println(mensaje);
		logger.info(mensaje);
		
		try {
			Usuario usuario = usuarioService.findByUsername(authentication.getName());
			if(usuario.getIntentos() == null) {
				usuario.setIntentos(0);
			}
			
			logger.info(":: Número de intentos actual es de [".concat(usuario.getIntentos().toString()).concat("] ::"));
			usuario.setIntentos(usuario.getIntentos()+1);
			logger.info(":: Número de intentos después de actualizar contador [".concat(usuario.getIntentos().toString()).concat("] ::"));
			
			if(usuario.getIntentos()>=3) {
				logger.error(String.format(":: El usuario %s deshabilitado por máximo de intentos ::", usuario.getUsername()));
				usuario.setEnabled(false);
			}
			
			usuarioService.update(usuario, usuario.getId());
			
		} catch (FeignException e) {
			logger.error(String.format(":: El usuario %s no existe en el sistema ::", authentication.getName()));
		}
		
	}

}
