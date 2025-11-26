using Explorify.Api.Likes.Domain.Entities;
using Explorify.Api.Likes.Infrastructure.Persistence;
using Explorify.Api.Likes.Infrastructure.Repositories;
using FluentAssertions;
using Testcontainers.MongoDb;
using Xunit;

namespace Explorify.Api.Likes.Tests.Integration.Repositories
{
    public class LikeRepositoryIntegrationTests : IAsyncLifetime
    {
        private MongoDbContainer _mongoContainer = null!;
        private MongoContext _context = null!;
        private LikeRepository _repository = null!;

        public async Task InitializeAsync()
        {
            _mongoContainer = new MongoDbBuilder()
                .WithImage("mongo:7.0")
                .Build();

            await _mongoContainer.StartAsync();

            var options = new MongoOptions
            {
                ConnectionString = _mongoContainer.GetConnectionString(),
                Database = "LikesTestDb",
                LikesCollection = "likes"
            };

            _context = new MongoContext(options);
            _repository = new LikeRepository(_context);
        }

        public async Task DisposeAsync()
        {
            await _mongoContainer.DisposeAsync();
        }

        [Fact]
        public async Task CreateAsync_ShouldAddLikeToDatabase()
        {
            // Arrange
            var like = new Like
            {
                PublicationId = "507f1f77bcf86cd799439011",
                UserId = "507f1f77bcf86cd799439012",
                CreatedAt = DateTime.UtcNow
            };

            // Act
            await _repository.CreateAsync(like);

            // Assert
            var retrieved = await _repository.GetByIdAsync(like.Id);
            retrieved.Should().NotBeNull();
            retrieved!.PublicationId.Should().Be(like.PublicationId);
            retrieved.UserId.Should().Be(like.UserId);
        }

        [Fact]
        public async Task GetByUserAndPublicationAsync_ShouldReturnLike_WhenExists()
        {
            // Arrange
            var like = new Like
            {
                PublicationId = "507f1f77bcf86cd799439011",
                UserId = "507f1f77bcf86cd799439012",
                CreatedAt = DateTime.UtcNow
            };
            await _repository.CreateAsync(like);

            // Act
            var result = await _repository.GetByUserAndPublicationAsync(like.UserId, like.PublicationId);

            // Assert
            result.Should().NotBeNull();
            result!.UserId.Should().Be(like.UserId);
            result.PublicationId.Should().Be(like.PublicationId);
        }

        [Fact]
        public async Task GetByUserAndPublicationAsync_ShouldReturnNull_WhenNotExists()
        {
            // Act
            var result = await _repository.GetByUserAndPublicationAsync("nonexistent", "nonexistent");

            // Assert
            result.Should().BeNull();
        }

        [Fact]
        public async Task GetByPublicationIdAsync_ShouldReturnAllLikesForPublication()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var likes = new[]
            {
                new Like { PublicationId = publicationId, UserId = "user1", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = publicationId, UserId = "user2", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "otherpub", UserId = "user3", CreatedAt = DateTime.UtcNow }
            };

            foreach (var like in likes)
            {
                await _repository.CreateAsync(like);
            }

            // Act
            var result = await _repository.GetByPublicationIdAsync(publicationId);

            // Assert
            result.Should().HaveCount(2);
            result.Should().AllSatisfy(l => l.PublicationId.Should().Be(publicationId));
        }

        [Fact]
        public async Task GetByUserIdAsync_ShouldReturnAllLikesForUser()
        {
            // Arrange
            var userId = "507f1f77bcf86cd799439012";
            var likes = new[]
            {
                new Like { PublicationId = "pub1", UserId = userId, CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "pub2", UserId = userId, CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "pub3", UserId = "otheruser", CreatedAt = DateTime.UtcNow }
            };

            foreach (var like in likes)
            {
                await _repository.CreateAsync(like);
            }

            // Act
            var result = await _repository.GetByUserIdAsync(userId);

            // Assert
            result.Should().HaveCount(2);
            result.Should().AllSatisfy(l => l.UserId.Should().Be(userId));
        }

        [Fact]
        public async Task CountByPublicationIdAsync_ShouldReturnCorrectCount()
        {
            // Arrange
            var publicationId = "507f1f77bcf86cd799439011";
            var likes = new[]
            {
                new Like { PublicationId = publicationId, UserId = "user1", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = publicationId, UserId = "user2", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = publicationId, UserId = "user3", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "otherpub", UserId = "user4", CreatedAt = DateTime.UtcNow }
            };

            foreach (var like in likes)
            {
                await _repository.CreateAsync(like);
            }

            // Act
            var count = await _repository.CountByPublicationIdAsync(publicationId);

            // Assert
            count.Should().Be(3);
        }

        [Fact]
        public async Task DeleteAsync_ShouldRemoveLike()
        {
            // Arrange
            var like = new Like
            {
                PublicationId = "507f1f77bcf86cd799439011",
                UserId = "507f1f77bcf86cd799439012",
                CreatedAt = DateTime.UtcNow
            };
            await _repository.CreateAsync(like);

            // Act
            await _repository.DeleteAsync(like.Id);

            // Assert
            var result = await _repository.GetByIdAsync(like.Id);
            result.Should().BeNull();
        }

        [Fact]
        public async Task DeleteByUserAndPublicationAsync_ShouldRemoveLike()
        {
            // Arrange
            var like = new Like
            {
                PublicationId = "507f1f77bcf86cd799439011",
                UserId = "507f1f77bcf86cd799439012",
                CreatedAt = DateTime.UtcNow
            };
            await _repository.CreateAsync(like);

            // Act
            await _repository.DeleteByUserAndPublicationAsync(like.UserId, like.PublicationId);

            // Assert
            var result = await _repository.GetByUserAndPublicationAsync(like.UserId, like.PublicationId);
            result.Should().BeNull();
        }

        [Fact]
        public async Task GetAllAsync_ShouldReturnAllLikes()
        {
            // Arrange
            var likes = new[]
            {
                new Like { PublicationId = "pub1", UserId = "user1", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "pub2", UserId = "user2", CreatedAt = DateTime.UtcNow },
                new Like { PublicationId = "pub3", UserId = "user3", CreatedAt = DateTime.UtcNow }
            };

            foreach (var like in likes)
            {
                await _repository.CreateAsync(like);
            }

            // Act
            var result = await _repository.GetAllAsync();

            // Assert
            result.Should().HaveCountGreaterOrEqualTo(3);
        }
    }
}