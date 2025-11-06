using Microsoft.EntityFrameworkCore;
using Comentarios.API.Models;

namespace Comentarios.API.Data;

public class ComentariosDbContext : DbContext
{
    public ComentariosDbContext(DbContextOptions<ComentariosDbContext> options)
        : base(options)
    {
    }

    public DbSet<Comentario> Comentarios { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Comentario>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Text).IsRequired().HasMaxLength(1000);
            entity.Property(e => e.UserId).IsRequired();
            entity.Property(e => e.PublicacionId).IsRequired();
        });
    }
}