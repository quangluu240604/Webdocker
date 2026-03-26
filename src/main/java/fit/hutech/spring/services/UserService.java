package fit.hutech.spring.services;

import fit.hutech.spring.constants.Provider;
import fit.hutech.spring.entities.User;
import fit.hutech.spring.repositories.IRoleRepository;
import fit.hutech.spring.repositories.IUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {
	@Autowired
	private IUserRepository userRepository;
	@Autowired
	private IRoleRepository roleRepository;

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
	public void save(@NotNull User user) {
		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		userRepository.save(user);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
	public void Save(@NotNull User user) {
		save(user);
	}

	public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public Optional<User> findByPhone(String phone) {
		return userRepository.findByPhone(phone);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
	public void setDefaultRole(String username) {
		var userOpt = userRepository.findByUsername(username);
		var roleOpt = roleRepository.findByName("USER");
		if (userOpt.isPresent() && roleOpt.isPresent()) {
			var user = userOpt.get();
			user.getRoles().add(roleOpt.get());
			userRepository.save(user);
		}
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
	public void setDefaultRole(@NotNull User user) {
		var roleOpt = roleRepository.findByName("USER");
		if (roleOpt.isPresent()) {
			user.getRoles().add(roleOpt.get());
			userRepository.save(user);
		}
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = {Exception.class, Throwable.class})
	public void saveOauthUser(String email, @NotNull String username) {
		if (email == null || email.isBlank()) {
			return;
		}
		var existingByEmail = userRepository.findByEmail(email);
		if (existingByEmail.isPresent()) {
			var existingUser = existingByEmail.get();
			if (existingUser.getRoles() == null || existingUser.getRoles().isEmpty()) {
				setDefaultRole(existingUser);
			}
			return;
		}
		String normalizedUsername = (username == null || username.isBlank()) ? email : username.trim();
		if (userRepository.findByUsername(normalizedUsername).isPresent()) {
			normalizedUsername = email;
		}
		var user = new User();
		user.setUsername(normalizedUsername);
		user.setEmail(email);
		user.setPassword(new BCryptPasswordEncoder().encode(normalizedUsername));
		user.setProvider(Provider.GOOGLE.value);
		userRepository.save(user);
		setDefaultRole(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getUsername())
				.password(user.getPassword())
				.authorities(user.getAuthorities())
				.accountExpired(false)
				.accountLocked(false)
				.credentialsExpired(false)
				.disabled(false)
				.build();
	}
}
