package LifeValuable.Library.security;

import LifeValuable.Library.model.Reader;
import LifeValuable.Library.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final ReaderRepository readerRepository;

    @Autowired
    public UserDetailsServiceImpl(ReaderRepository readerRepository) {
        this.readerRepository = readerRepository;
    }

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Reader reader = readerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Не найден пользователь с email: " + email));

        return User.builder()
                .username(reader.getEmail())
                .password(reader.getPassword())
                .roles(reader.getRole().name())
                .build();
    }
}