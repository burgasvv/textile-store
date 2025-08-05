package org.burgas.backendserver.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.burgas.backendserver.dto.bill.BillResponse;
import org.burgas.backendserver.dto.bucket.BucketResponse;
import org.burgas.backendserver.dto.product.ProductOrder;
import org.burgas.backendserver.entity.*;
import org.burgas.backendserver.exception.*;
import org.burgas.backendserver.mapper.BucketMapper;
import org.burgas.backendserver.mapper.CategoryMapper;
import org.burgas.backendserver.message.BucketMessages;
import org.burgas.backendserver.message.IdentityMessages;
import org.burgas.backendserver.message.ProductMessages;
import org.burgas.backendserver.repository.bucket.BucketMasterRepository;
import org.burgas.backendserver.repository.bucket.BucketProductMasterRepository;
import org.burgas.backendserver.repository.bucket.BucketProductReplicaRepository;
import org.burgas.backendserver.repository.bucket.BucketReplicaRepository;
import org.burgas.backendserver.repository.identity.IdentityMasterRepository;
import org.burgas.backendserver.repository.identity.IdentityReplicaRepository;
import org.burgas.backendserver.repository.product.ProductMasterRepository;
import org.burgas.backendserver.repository.product.ProductReplicaRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS, transactionManager = "replicaPostgresTransactionManager")
public class BucketService {

    private final BucketMasterRepository bucketMasterRepository;
    private final BucketReplicaRepository bucketReplicaRepository;

    private final BucketMapper bucketMapper;
    private final BeanFactory beanFactory;

    private final IdentityMasterRepository identityMasterRepository;
    private final IdentityReplicaRepository identityReplicaRepository;

    private final BucketProductMasterRepository bucketProductMasterRepository;
    private final BucketProductReplicaRepository bucketProductReplicaRepository;

    private final ProductMasterRepository productMasterRepository;
    private final ProductReplicaRepository productReplicaRepository;

    private final CategoryMapper categoryMapper;
    private final BillService billService;

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse findByCookie(final HttpServletRequest request) {
        Cookie bucketCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("bucket-cookie-id"))
                .findFirst()
                .orElse(null);
        if (bucketCookie == null)
            throw new BucketCookieNotFoundException(BucketMessages.BUCKET_COOKIE_NOT_FOUND.getMessages());

        Bucket bucket = this.getBucketByCookieValue(bucketCookie);
        return this.bucketMapper.toBucketResponse(bucket);
    }

    public Bucket addNewOrFindBucketByCookie(final HttpServletRequest request, final HttpServletResponse response) {
        Cookie bucketCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("bucket-cookie-id"))
                .findFirst()
                .orElse(null);

        if (bucketCookie == null) {
            UUID bucketId = UUID.randomUUID();
            while (this.bucketMasterRepository.existsById(bucketId) && this.bucketReplicaRepository.existsById(bucketId))
                bucketId = UUID.randomUUID();

            Bucket bucket = Bucket.builder()
                    .id(bucketId)
                    .identity(null)
                    .cost(0.0)
                    .build();

            Cookie cookie = new Cookie("bucket-cookie-id", String.valueOf(bucket.getId()));
            cookie.setMaxAge(-1);
            response.addCookie(cookie);

            Bucket saved = this.bucketMasterRepository.save(bucket);
            this.beanFactory.getBean(BucketService.class).findByCookieCreateBucketInReplica(bucket);

            return saved;
        }

        return this.getBucketByCookieValue(bucketCookie);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void findByCookieCreateBucketInReplica(final Bucket bucket) {
        this.bucketReplicaRepository.save(bucket);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public Bucket getBucketByCookieValue(final Cookie bucketCookie) {
        return this.bucketReplicaRepository.findById(UUID.fromString(bucketCookie.getValue()))
                .orElseThrow(() -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages()));
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse addProductByCookieToMaster(final HttpServletRequest request, final HttpServletResponse response, final UUID productId) {
        Bucket bucket = this.addNewOrFindBucketByCookie(request, response);
        Product product = this.productMasterRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = BucketProduct.builder()
                .bucketId(bucket.getId())
                .productId(product.getId())
                .amount(1L)
                .build();
        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());

        bucket.setCost(product.getPrice());
        Bucket saved = this.bucketMasterRepository.save(bucket);
        this.bucketProductMasterRepository.save(bucketProduct);

        this.beanFactory.getBean(BucketService.class).addProductByCookieToReplica(bucket, productId);

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void addProductByCookieToReplica(final Bucket bucket, final UUID productId) {
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = BucketProduct.builder()
                .bucketId(bucket.getId())
                .productId(product.getId())
                .amount(1L)
                .build();
        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());
        bucket.setCost(product.getPrice());

        this.bucketReplicaRepository.save(bucket);
        this.bucketProductReplicaRepository.save(bucketProduct);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse removeProductByCookieFromMaster(final HttpServletRequest request, final HttpServletResponse response, final UUID productId) {
        Bucket bucket = this.addNewOrFindBucketByCookie(request, response);
        Product product = this.productMasterRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucket.setCost(bucket.getCost() - (product.getPrice() * bucketProduct.getAmount()));

        this.bucketProductMasterRepository.delete(bucketProduct);
        Bucket saved = this.bucketMasterRepository.save(bucket);

        this.beanFactory.getBean(BucketService.class).removeProductByCookieFromReplica(bucket, product.getId());

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void removeProductByCookieFromReplica(final Bucket bucket, final UUID productId) {
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        this.bucketProductReplicaRepository.delete(bucketProduct);
        this.bucketReplicaRepository.save(bucket);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse plusProductAmountByCookieInMaster(final HttpServletRequest request, final HttpServletResponse response, final UUID productId) {
        Bucket bucket = this.addNewOrFindBucketByCookie(request, response);
        Product product = this.productMasterRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() + 1);
        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());
        bucket.setCost(bucket.getCost() + product.getPrice());

        this.bucketProductMasterRepository.save(bucketProduct);
        Bucket saved = this.bucketMasterRepository.save(bucket);

        this.beanFactory.getBean(BucketService.class).plusProductAmountByCookieInReplica(bucket, product.getId());

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void plusProductAmountByCookieInReplica(final Bucket bucket, final UUID productId) {
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() + 1);
        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());

        this.bucketProductReplicaRepository.save(bucketProduct);
        this.bucketReplicaRepository.save(bucket);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse minusProductAmountByCookieInMaster(final HttpServletRequest request, final HttpServletResponse response, final UUID productId) {
        Bucket bucket = this.addNewOrFindBucketByCookie(request, response);
        Product product = this.productMasterRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() - 1);
        if (bucketProduct.getAmount() <= 0)
            throw new WrongProductAmountException(ProductMessages.WRONG_PRODUCT_AMOUNT.getMessage());

        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());
        bucket.setCost(bucket.getCost() - product.getPrice());

        this.bucketProductMasterRepository.save(bucketProduct);
        Bucket saved = this.bucketMasterRepository.save(bucket);

        this.beanFactory.getBean(BucketService.class).minusProductAmountByCookieInReplica(bucket, product.getId());

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void minusProductAmountByCookieInReplica(final Bucket bucket, final UUID productId) {
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(bucket.getId(), product.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() - 1);
        if (bucketProduct.getAmount() <= 0)
            throw new WrongProductAmountException(ProductMessages.WRONG_PRODUCT_AMOUNT.getMessage());

        bucketProduct.setCost(product.getPrice() * bucketProduct.getAmount());

        this.bucketProductReplicaRepository.save(bucketProduct);
        this.bucketReplicaRepository.save(bucket);
    }

    public BucketResponse findInSession(final HttpServletRequest httpServletRequest) {
        HttpSession session = httpServletRequest.getSession();
        BucketResponse bucketResponse = (BucketResponse) session.getAttribute("bucket-session");
        if (bucketResponse == null) {
            bucketResponse = BucketResponse.builder().cost(0.0)
                    .id(UUID.randomUUID())
                    .products(new ArrayList<>())
                    .build();
            session.setAttribute("bucket-session", bucketResponse);
        }
        return bucketResponse;
    }

    public BucketResponse addProductToBucketInSession(final HttpServletRequest httpServletRequest, final UUID productId) {
        HttpSession session = httpServletRequest.getSession();
        BucketResponse bucketResponse = this.findInSession(httpServletRequest);
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        if (
                !bucketResponse.getProducts().stream().map(ProductOrder::getId)
                        .toList()
                        .contains(product.getId())
        ) {
            bucketResponse.setCost(bucketResponse.getCost() + product.getPrice());
            bucketResponse.getProducts().add(
                    ProductOrder.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .category(
                                    Optional.ofNullable(product.getCategory())
                                            .map(this.categoryMapper::toCategoryWithoutProducts)
                                            .orElse(null)
                            )
                            .amount(1L)
                            .images(product.getImages())
                            .build()
            );
            session.setAttribute("bucket-session", bucketResponse);
            return bucketResponse;

        } else {
            throw new ProductNotContainsInBucketFromSessionException(ProductMessages.PRODUCT_CONTAINS_IN_BUCKET.getMessage());
        }
    }

    public BucketResponse removeProductFromBucketInSession(final HttpServletRequest httpServletRequest, final UUID productId) {
        HttpSession session = httpServletRequest.getSession();
        BucketResponse bucketResponse = this.findInSession(httpServletRequest);
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        if (
                bucketResponse.getProducts().stream().map(ProductOrder::getId)
                        .toList()
                        .contains(product.getId())
        ) {
            LinkedList<ProductOrder> productOrders = new LinkedList<>();
            bucketResponse.setCost(bucketResponse.getCost() - product.getPrice());
            bucketResponse.getProducts()
                    .stream()
                    .filter(productOrder -> productOrder.getId().equals(product.getId()))
                    .forEach(productOrders::add);
            bucketResponse.getProducts().remove(productOrders.getFirst());
            session.setAttribute("bucket-session", bucketResponse);
            return bucketResponse;

        } else {
            throw new ProductNotContainsInBucketFromSessionException(ProductMessages.PRODUCT_NOT_CONTAINS_IN_BUCKET.getMessage());
        }
    }

    public BucketResponse plusProductAmountInSession(final HttpServletRequest httpServletRequest, final UUID productId) {
        HttpSession session = httpServletRequest.getSession();
        BucketResponse bucketResponse = this.findInSession(httpServletRequest);
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        if (
                bucketResponse.getProducts().stream().map(ProductOrder::getId)
                        .toList()
                        .contains(product.getId())
        ) {
            bucketResponse.setCost(bucketResponse.getCost() + product.getPrice());
            bucketResponse.getProducts()
                    .stream()
                    .filter(productOrder -> productOrder.getId().equals(product.getId()))
                    .forEach(
                            productOrder -> {
                                productOrder.setAmount(productOrder.getAmount() + 1);
                                productOrder.setPrice(product.getPrice() * productOrder.getAmount());
                            }
                    );
            session.setAttribute("bucket-session", bucketResponse);
            return bucketResponse;

        } else {
            throw new ProductNotContainsInBucketFromSessionException(ProductMessages.PRODUCT_NOT_CONTAINS_IN_BUCKET.getMessage());
        }
    }

    public BucketResponse minusProductAmountInSession(final HttpServletRequest httpServletRequest, final UUID productId) {
        HttpSession session = httpServletRequest.getSession();
        BucketResponse bucketResponse = this.findInSession(httpServletRequest);
        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        if (
                bucketResponse.getProducts().stream().map(ProductOrder::getId)
                        .toList()
                        .contains(product.getId())
        ) {
            bucketResponse.setCost(bucketResponse.getCost() - product.getPrice());
            bucketResponse.getProducts()
                    .stream()
                    .filter(productOrder -> productOrder.getId().equals(product.getId()))
                    .forEach(
                            productOrder -> {
                                productOrder.setAmount(productOrder.getAmount() - 1);
                                if (productOrder.getAmount() <= 0)
                                    throw new WrongProductAmountException(ProductMessages.WRONG_PRODUCT_AMOUNT.getMessage());

                                productOrder.setPrice(product.getPrice() * productOrder.getAmount());
                            }
                    );
            session.setAttribute("bucket-session", bucketResponse);
            return bucketResponse;

        } else {
            throw new ProductNotContainsInBucketFromSessionException(ProductMessages.PRODUCT_NOT_CONTAINS_IN_BUCKET.getMessage());
        }
    }

    public BucketResponse findById(final UUID bucketId) {
        return this.bucketReplicaRepository.findById(bucketId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : bucketId)
                .map(this.bucketMapper::toBucketResponse)
                .orElseThrow(
                        () -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages())
                );
    }

    public BucketResponse findBucketByIdentityId(final UUID identityId) {
        return this.bucketReplicaRepository.findBucketByIdentityId(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .map(this.bucketMapper::toBucketResponse)
                .orElseThrow(
                        () -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages())
                );

    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public void createBucketInMaster(final Identity identity) {
        UUID bucketId = UUID.randomUUID();
        while (this.bucketMasterRepository.existsById(bucketId) && this.bucketReplicaRepository.existsById(bucketId))
            bucketId = UUID.randomUUID();

        Bucket bucket = Bucket.builder()
                .id(bucketId)
                .identity(identity)
                .cost(0.0)
                .build();

        this.bucketMasterRepository.save(bucket);
        this.beanFactory.getBean(BucketService.class).createBucketInReplica(bucket);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void createBucketInReplica(final Bucket bucket) {
        this.bucketReplicaRepository.save(bucket);
    }

    @Transactional(
            readOnly = true, propagation = Propagation.SUPPORTS,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public Triple<Identity, Bucket, Product> getIdentityBucketProductFromMaster(final UUID identityId, final UUID productId) {
        Identity identity = this.identityMasterRepository.findById(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        Bucket bucket = this.bucketMasterRepository.findById(identity.getBucket().getId())
                .orElseThrow(() -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages()));

        Product product = this.productMasterRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        return new Triple<>(identity, bucket, product);
    }

    @Transactional(
            readOnly = true, propagation = Propagation.SUPPORTS,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public Triple<Identity, Bucket, Product> getIdentityBucketProductFromReplica(final UUID identityId, final UUID productId) {
        Identity identity = this.identityReplicaRepository.findById(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        Bucket bucket = this.bucketReplicaRepository.findById(identity.getBucket().getId())
                .orElseThrow(() -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages()));

        Product product = this.productReplicaRepository.findById(productId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : productId)
                .orElseThrow(() -> new ProductNotFoundException(ProductMessages.PRODUCT_NOT_FOUND.getMessage()));

        return new Triple<>(identity, bucket, product);
    }

    @Transactional(
            readOnly = true, propagation = Propagation.SUPPORTS,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public Pair<Identity, Bucket> getIdentityBucketFromMaster(final UUID identityId) {
        Identity identity = this.identityMasterRepository.findById(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        Bucket bucket = this.bucketMasterRepository.findById(identity.getBucket().getId())
                .orElseThrow(() -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages()));

        return new Pair<>(identity, bucket);
    }

    @Transactional(
            readOnly = true, propagation = Propagation.SUPPORTS,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public Pair<Identity, Bucket> getIdentityBucketFromReplica(final UUID identityId) {
        Identity identity = this.identityReplicaRepository.findById(identityId == null ?
                        UUID.nameUUIDFromBytes("0".getBytes(StandardCharsets.UTF_8)) : identityId)
                .orElseThrow(() -> new IdentityNotFoundException(IdentityMessages.IDENTITY_NOT_FOUND.getMessage()));

        Bucket bucket = this.bucketReplicaRepository.findById(identity.getBucket().getId())
                .orElseThrow(() -> new BucketNotFoundException(BucketMessages.BUCKET_NOT_FOUND.getMessages()));

        return new Pair<>(identity, bucket);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse addProductToMaster(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromMaster(identityId, productId);

        BucketProduct bucketProduct = BucketProduct.builder()
                .bucketId(triple.b.getId())
                .productId(triple.c.getId())
                .amount(1L)
                .build();
        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());

        triple.b.setCost(triple.b.getCost() + triple.c.getPrice());
        Bucket saved = this.bucketMasterRepository.save(triple.b);

        this.bucketProductMasterRepository.save(bucketProduct);
        this.beanFactory.getBean(BucketService.class).addProductToReplica(identityId, productId);

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void addProductToReplica(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromReplica(identityId, productId);

        BucketProduct bucketProduct = BucketProduct.builder()
                .bucketId(triple.b.getId())
                .productId(triple.c.getId())
                .amount(1L)
                .build();
        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());

        triple.b.setCost(triple.b.getCost() + triple.c.getPrice());
        this.bucketReplicaRepository.save(triple.b);

        this.bucketProductReplicaRepository.save(bucketProduct);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse plusAmountOnMaster(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromMaster(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() + 1L);
        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());
        this.bucketProductMasterRepository.save(bucketProduct);

        triple.b.setCost(triple.b.getCost() + triple.c.getPrice());
        this.bucketMasterRepository.save(triple.b);

        this.beanFactory.getBean(BucketService.class).plusAmountOnReplica(identityId, productId);

        return this.bucketMapper.toBucketResponse(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void plusAmountOnReplica(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromReplica(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() + 1L);
        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());
        this.bucketProductReplicaRepository.save(bucketProduct);

        triple.b.setCost(triple.b.getCost() + triple.c.getPrice());
        this.bucketReplicaRepository.save(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse minusAmountOnMaster(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromMaster(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() - 1L);
        if (bucketProduct.getAmount() <= 0)
            throw new WrongProductAmountException(ProductMessages.WRONG_PRODUCT_AMOUNT.getMessage());

        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());
        this.bucketProductMasterRepository.save(bucketProduct);

        triple.b.setCost(triple.b.getCost() - triple.c.getPrice());
        this.bucketMasterRepository.save(triple.b);

        this.beanFactory.getBean(BucketService.class).minusAmountOnReplica(identityId, productId);

        return this.bucketMapper.toBucketResponse(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void minusAmountOnReplica(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromReplica(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        bucketProduct.setAmount(bucketProduct.getAmount() - 1L);
        if (bucketProduct.getAmount() <= 0)
            throw new WrongProductAmountException(ProductMessages.WRONG_PRODUCT_AMOUNT.getMessage());

        bucketProduct.setCost(triple.c.getPrice() * bucketProduct.getAmount());
        this.bucketProductReplicaRepository.save(bucketProduct);

        triple.b.setCost(triple.b.getCost() - triple.c.getPrice());
        this.bucketReplicaRepository.save(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse removeProductFromBucketFromMaster(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromMaster(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductMasterRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        triple.b.setCost(triple.b.getCost() - (triple.c.getPrice() * bucketProduct.getAmount()));
        this.bucketProductMasterRepository.deleteById(
                BucketProductPK.builder()
                        .bucketId(bucketProduct.getBucketId())
                        .productId(bucketProduct.getProductId())
                        .build()
        );
        this.bucketMasterRepository.save(triple.b);
        this.beanFactory.getBean(BucketService.class).removeProductFromBucketFromReplica(identityId, productId);

        return this.bucketMapper.toBucketResponse(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void removeProductFromBucketFromReplica(final UUID identityId, final UUID productId) {
        Triple<Identity, Bucket, Product> triple = this.getIdentityBucketProductFromReplica(identityId, productId);
        BucketProduct bucketProduct = this.bucketProductReplicaRepository.findBucketProductByBucketIdAndProductId(triple.b.getId(), triple.c.getId())
                .orElseThrow(() -> new BucketProductNotFoundException(BucketMessages.BUCKET_PRODUCT_NOT_FOUND.getMessages()));

        triple.b.setCost(triple.b.getCost() - (triple.c.getPrice() * bucketProduct.getAmount()));
        this.bucketProductReplicaRepository.deleteById(
                BucketProductPK.builder()
                        .bucketId(bucketProduct.getBucketId())
                        .productId(bucketProduct.getProductId())
                        .build()
        );
        this.bucketReplicaRepository.save(triple.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BucketResponse cleanBucketOnMaster(final UUID identityId) {
        Pair<Identity, Bucket> pair = this.getIdentityBucketFromMaster(identityId);
        this.bucketProductMasterRepository.deleteBucketProductsByBucketId(pair.b.getId());

        pair.b.setCost(0.0);
        Bucket saved = this.bucketMasterRepository.save(pair.b);

        this.beanFactory.getBean(BucketService.class).cleanBucketOnReplica(pair.a.getId());

        return this.bucketMapper.toBucketResponse(saved);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.NESTED,
            rollbackFor = Exception.class, transactionManager = "replicaPostgresTransactionManager"
    )
    public void cleanBucketOnReplica(final UUID identityId) {
        Pair<Identity, Bucket> pair = this.getIdentityBucketFromReplica(identityId);
        this.bucketProductReplicaRepository.deleteBucketProductsByBucketId(pair.b.getId());

        pair.b.setCost(0.0);
        this.bucketReplicaRepository.save(pair.b);
    }

    @Transactional(
            isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED,
            rollbackFor = Exception.class, transactionManager = "masterPostgresTransactionManager"
    )
    public BillResponse payBill(final UUID identityId) {
        Pair<Identity, Bucket> pair = this.getIdentityBucketFromMaster(identityId);
        BillResponse billResponse = this.billService.createBillOnMaster(
                this.bucketMapper.toBucketResponse(pair.b)
        );
        this.beanFactory.getBean(BucketService.class).cleanBucketOnMaster(pair.a.getId());
        return billResponse;
    }
}
