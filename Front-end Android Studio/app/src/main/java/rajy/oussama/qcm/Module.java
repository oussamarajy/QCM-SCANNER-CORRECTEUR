package rajy.oussama.qcm;

public class Module {

    public Integer id_module;
    public String designation;
    public String answers;

    public Module(Integer id_module, String designation, String answers) {
        this.id_module = id_module;
        this.designation = designation;
        this.answers = answers;
    }

    public Module(){}

    public Integer getId_module() {
        return id_module;
    }

    public String getDesignation() {
        return designation;
    }

    public String getAnswers() {
        return answers;
    }

    public void setId_module(Integer id_module) {
        this.id_module = id_module;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
}
