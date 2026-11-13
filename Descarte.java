public class Descarte {
    private Long id;
    private String nomeMorador;
    private String telefone;
    private String bairro;
    private String tipoMaterial;
    private String quantidade;
    private String status;
    private String nomeColetor;

    public Descarte(Long id, String nomeMorador, String telefone, String bairro, String tipoMaterial, String quantidade) {
        this.id = id;
        this.nomeMorador = nomeMorador;
        this.telefone = telefone;
        this.bairro = bairro;
        this.tipoMaterial = tipoMaterial;
        this.quantidade = quantidade;
        this.status = "DISPONIVEL";
        this.nomeColetor = "Nenhum";
    }

    public Long getId() { return id; }
    public String getNomeMorador() { return nomeMorador; }
    public String getTelefone() { return telefone; }
    public String getBairro() { return bairro; }
    public String getTipoMaterial() { return tipoMaterial; }
    public String getQuantidade() { return quantidade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNomeColetor() { return nomeColetor; }
    public void setNomeColetor(String nomeColetor) { this.nomeColetor = nomeColetor; }
}