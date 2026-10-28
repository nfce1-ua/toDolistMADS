package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    EquipoRepository equipoRepository;

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        comprobarNombre(nombre);
        Equipo equipo = new Equipo(nombre);
        equipoRepository.save(equipo);
        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = buscarEquipo(id);
        if (equipo == null) {
            // Cambiar a EquipoServiceException
            throw new EquipoServiceException("Equipo " + id + " no existe");
        }
        return modelMapper.map(equipo, EquipoData.class);
    }

    public List<EquipoData> findAllOrdenadoPorNombre() {
        return equipoRepository.findAll().stream()
                .sorted((e1, e2) -> e1.getNombre().compareTo(e2.getNombre()))
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long id, Long id1) {
        Equipo equipo = buscarEquipo(id);
        Usuario usuario = buscarUsuario(id1);
        if (equipo == null) {
            throw new EquipoServiceException("Equipo " + id + " no existe");
        }
        if (usuario == null) {
            throw new EquipoServiceException("Usuario " + id1 + " no existe");
        }
        if (equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario " + id1
                    + " ya pertenece al equipo " + id);
        }
        equipo.addUsuario(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = buscarEquipo(id);
        if (equipo == null) {
            throw new EquipoServiceException("Equipo " + id + " no existe");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long id) {
        Usuario usuario = buscarUsuario(id);
        if (usuario == null) {
            throw new EquipoServiceException("Usuario " + id + " no existe");
        }
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
    }
    @Transactional
    public void eliminarUsuarioDeEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = buscarEquipo(idEquipo);
        Usuario usuario = buscarUsuario(idUsuario);
        if (equipo == null) {
            throw new EquipoServiceException("Equipo " + idEquipo + " no existe");
        }
        if (usuario == null) {
            throw new EquipoServiceException("Usuario " + idUsuario + " no existe");
        }
        if (!equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario " + idUsuario
                    + " no pertenece al equipo " + idEquipo);
        }
        equipo.removeUsuario(usuario);
    }

    private Equipo buscarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null) {
            throw new EquipoServiceException("Equipo " + id + " no existe");
        }
        return equipo;
    }

    private Usuario buscarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new EquipoServiceException("Usuario " + id + " no existe");
        }
        return usuario;
    }

    @Transactional(readOnly = true)
    public boolean usuarioPerteneceAEquipo(Long idEquipo, Long idUsuario) {
        Equipo equipo = buscarEquipo(idEquipo);
        Usuario usuario = buscarUsuario(idUsuario);
        return equipo.getUsuarios().contains(usuario);
    }

    @Transactional
    public EquipoData renombrarEquipo(Long id, String nombre) {
        Equipo equipo = buscarEquipo(id);
        comprobarNombre(nombre);
        equipo.setNombre(nombre);
        return modelMapper.map(equipo, EquipoData.class);
    }

    private void comprobarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío");
        }
    }
}