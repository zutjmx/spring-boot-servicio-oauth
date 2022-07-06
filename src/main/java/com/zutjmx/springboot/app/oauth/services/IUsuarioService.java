package com.zutjmx.springboot.app.oauth.services;

import com.zutjmx.springboot.app.commons.usuarios.models.entity.Usuario;

public interface IUsuarioService {
	
	public Usuario findByUsername(String username);

}
