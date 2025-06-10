package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.dto.cache.CacheablePage;
import LifeValuable.Library.exception.BookNotFoundException;
import LifeValuable.Library.exception.GenreNotFoundException;
import LifeValuable.Library.mapper.BookMapper;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.repository.BookRepository;
import LifeValuable.Library.repository.GenreRepository;
import LifeValuable.Library.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CacheConfig(cacheNames = "books")
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final GenreRepository genreRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper, GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.genreRepository = genreRepository;
    }

    @CachePut(key = "#result.id")
    @Override
    public BookDetailDTO create(CreateBookDTO createBookDTO) {
        if (bookRepository.findByIsbn(createBookDTO.isbn()).isPresent())
            throw new RuntimeException("A book with ISBN " + createBookDTO.isbn() + " already exists");

        if (createBookDTO.genreNames() == null || createBookDTO.genreNames().isEmpty())
            throw new RuntimeException("Book must have at least one genre");

        List<String> requestedGenreNames = createBookDTO.genreNames();
        List<Genre> foundGenres = genreRepository.findByNameIn(requestedGenreNames);

        if (foundGenres.size() != requestedGenreNames.size()) {
            Set<String> foundNames = foundGenres.stream().map(Genre::getName).collect(Collectors.toSet());
            Set<String> missingNames = new HashSet<>(requestedGenreNames);
            missingNames.removeAll(foundNames);
            throw new GenreNotFoundException("Genres not found: " + missingNames);
        }

        Book bookToSave = bookMapper.toEntity(createBookDTO);
        bookToSave.setGenres(foundGenres);
        Book savedBook = bookRepository.save(bookToSave);

        return bookMapper.toDetailDto(savedBook);
    }

    @CachePut(key = "#result.id")
    @Transactional
    @Override
    public BookDetailDTO update(CreateBookDTO createBookDTO, Long id) {
        Book bookToUpdate = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        if (bookRepository.findByIsbn(createBookDTO.isbn()).isPresent())
            throw new RuntimeException("A book with ISBN " + createBookDTO.isbn() + " already exists");

        bookToUpdate.setAuthor(createBookDTO.author());
        bookToUpdate.setTitle(createBookDTO.title());
        bookToUpdate.setIsbn(createBookDTO.isbn());
        bookToUpdate.setStock(createBookDTO.stock());
        bookToUpdate.setPublicationYear(createBookDTO.publicationYear());

        List<String> requestedGenreNames = createBookDTO.genreNames();
        List<Genre> foundGenres = genreRepository.findByNameIn(requestedGenreNames);

        if (foundGenres.size() != requestedGenreNames.size()) {
            Set<String> foundNames = foundGenres.stream().map(Genre::getName).collect(Collectors.toSet());
            Set<String> missingNames = new HashSet<>(requestedGenreNames);
            missingNames.removeAll(foundNames);
            throw new GenreNotFoundException("Genres not found: " + missingNames);
        }

        bookToUpdate.setGenres(foundGenres);

        return bookMapper.toDetailDto(bookToUpdate);
    }

    @CacheEvict
    @Override
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id))
            throw new BookNotFoundException(id);

        bookRepository.deleteById(id);
    }

    @Cacheable
    @Transactional
    @Override
    public BookDetailDTO findById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        return bookMapper.toDetailDto(book);
    }

    @Override
    public Book findModelById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    @Cacheable
    @Transactional
    @Override
    public Optional<BookDetailDTO> findByTitle(String title) {
        return bookRepository.findByTitle(title).map(bookMapper::toDetailDto);
    }

    @Cacheable
    @Transactional
    @Override
    public Optional<BookDetailDTO> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn).map(bookMapper::toDetailDto);
    }

    @Cacheable
    @Override
    public boolean isAvailableForLending(Long bookId) {
        return getAvailableStockCount(bookId) > 0;
    }

    @Cacheable
    @Override
    public int getAvailableStockCount(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        long activeLendingsCount = book.getLendings().stream()
                .filter(lending -> lending.getStatus().isActive())
                .count();
        return (int)(book.getStock() - activeLendingsCount);
    }

    @Override
    public Page<BookDTO> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByTitleContaining(String titleFragment, Pageable pageable) {
        return bookRepository.findByTitleContaining(titleFragment, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthor(author, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByPublicationYear(Integer year, Pageable pageable) {
        return bookRepository.findByPublicationYear(year, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable) {
        if (startYear > endYear)
            throw new RuntimeException("Start year must be less than or equal to end year");

        return bookRepository.findByPublicationYearBetween(startYear, endYear, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByGenreName(String genreName, Pageable pageable) {
        return bookRepository.findByGenreName(genreName, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByAllGenres(List<String> genreNames, Pageable pageable) {
        if (genreNames.isEmpty())
            throw new RuntimeException("Genre names list cannot be empty");

        List<Genre> genres = genreRepository.findByNameIn(genreNames);
        return bookRepository.findByAllGenres(genres, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByStockAvailable(Pageable pageable) {
        return bookRepository.findByStockGreaterThan(0, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByStockGreaterThan(Integer minStock, Pageable pageable) {
        return bookRepository.findByStockGreaterThan(minStock, pageable).map(bookMapper::toDto);
    }

    @Override
    public Page<BookDTO> findByStockLessThan(Integer maxStock, Pageable pageable) {
        return bookRepository.findByStockLessThan(maxStock, pageable).map(bookMapper::toDto);
    }
    
    @CachePut(key = "#result.id")
    @Transactional
    @Override
    public BookDetailDTO addGenreToBook(Long bookId, String genreName) {
        Book bookToUpdate = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
        List<Genre> genres = bookToUpdate.getGenres();
        if (genres.stream().noneMatch(genre -> genre.getName().equals(genreName))) {
            Genre genreToAdd = genreRepository.findByName(genreName).orElseThrow(() -> new GenreNotFoundException(genreName));
            genres.add(genreToAdd);
            bookToUpdate.setGenres(genres);
        }

        return bookMapper.toDetailDto(bookToUpdate);
    }

    @CachePut(key = "#result.id")
    @Transactional
    @Override
    public BookDetailDTO removeGenreFromBook(Long bookId, String genreName) {
        Book bookToUpdate = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        List<Genre> genres = bookToUpdate.getGenres();
        List<Genre> newGenres = genres.stream().filter(genre -> !genre.getName().equals(genreName)).collect(Collectors.toList());
        if (genres.size() == newGenres.size())
            throw new RuntimeException(String.format("Book does not have genre: %s", genreName));
        else if (newGenres.isEmpty())
            throw new RuntimeException("Can't remove the last genre from a book");

        bookToUpdate.setGenres(newGenres);

        return bookMapper.toDetailDto(bookToUpdate);
    }

    @CachePut(key = "#result.id")
    @Transactional
    @Override
    public BookDetailDTO updateBookStock(Long bookId, Integer newStock) {
        Book bookToUpdate = bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        if (newStock < 0)
            throw new RuntimeException("Can't set book stock less than zero");

        bookToUpdate.setStock(newStock);

        return bookMapper.toDetailDto(bookToUpdate);
    }
}
