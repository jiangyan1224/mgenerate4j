package uk.dioxic.mgenerate.apt.poet;

import com.squareup.javapoet.*;
import org.bson.Document;
import uk.dioxic.mgenerate.apt.model.PojoPropertyModel;
import uk.dioxic.mgenerate.apt.util.ModelUtil;
import uk.dioxic.mgenerate.common.Resolvable;
import uk.dioxic.mgenerate.common.TransformerRegistry;
import uk.dioxic.mgenerate.common.annotation.PojoProperty;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class PojoGeneratorPoet implements Poet {

    private final static String WRAPPER_SERVICE = "wrapperService";
    private final TypeElement typeElement;
    private final String packageName;
    private final String className;
    private final ClassName thisType;

    public PojoGeneratorPoet(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.packageName = ModelUtil.elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        this.className = typeElement.getSimpleName() + "Generator";
        this.thisType = ClassName.get(typeElement);
    }

    @Override
    public CharSequence getFullyQualifiedName() {
        if (this.packageName != null && this.packageName.trim().length() > 0) {
            return this.packageName + "." + this.className;
        }
        return this.className;
    }

    @Override
    public void generate(Appendable appendable) throws IOException {
        ClassName builderInterface = ClassName.get(Resolvable.class);
        List<PojoPropertyModel> properties = getProperties();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(builderInterface, this.thisType));

        addProperties(classBuilder, properties);

        addConstructor(classBuilder, properties);

        addResolveMethod(classBuilder, properties);

        TypeSpec classSpec = classBuilder.build();

        JavaFile javaFile = JavaFile.builder(this.packageName, classSpec).build();
        javaFile.writeTo(appendable);
    }

    private void addProperties(TypeSpec.Builder classBuilder, List<PojoPropertyModel> properties) {
        for (PojoPropertyModel field : properties) {
            classBuilder.addField(field.getResolvableTypeName(), field.getName(), Modifier.PRIVATE);
        }
    }

    private List<PojoPropertyModel> getProperties() {
        return typeElement
                .getEnclosedElements().stream().filter(this::filterProperties)
                .map(PojoPropertyModel::new)
                .collect(Collectors.toList());
    }

    private boolean filterProperties(Element el) {
        return el.getKind() == ElementKind.FIELD
                && !el.getModifiers().contains(Modifier.PRIVATE)
                && el.getAnnotation(PojoProperty.class) != null;
    }

    private void addConstructor(TypeSpec.Builder classBuilder, List<PojoPropertyModel> properties) {
        CodeBlock.Builder getBlock = CodeBlock.builder();

        for (PojoPropertyModel property : properties) {
            if (property.isSubDoc()) {
                getBlock.addStatement("$L = new $TGenerator(document.get($S, $T.class))", property.getName(), property.getRootTypeName(), property.getDocKey(), Document.class);
            }
            else {
                if (property.isRootTypeNameParameterized()) {
                    getBlock.addStatement("$L = ($T)$L.wrap(document,$S,$T.class)", property.getName(), Resolvable.class, WRAPPER_SERVICE, property.getDocKey(), property.getRootTypeNameErasure());
                }
                else {
                    getBlock.addStatement("$L = $L.wrap(document,$S,$T.class)", property.getName(), WRAPPER_SERVICE, property.getDocKey(), property.getRootTypeName());
                }
            }
        }

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Document.class, "document")
                .addParameter(TypeName.get(TransformerRegistry.class), WRAPPER_SERVICE)
                .addStatement("this.$L = $L", WRAPPER_SERVICE, WRAPPER_SERVICE)
                .addCode(getBlock.build())
                .build();

        classBuilder.addMethod(constructor);
    }

    private void addResolveMethod(TypeSpec.Builder classBuilder, List<PojoPropertyModel> properties) {

        CodeBlock.Builder setBlock = CodeBlock.builder();

        for (PojoPropertyModel property : properties) {
            setBlock.addStatement("tx.$L = $L.resolve()", property.getName(), property.getName());
        }

        MethodSpec method = MethodSpec.methodBuilder("resolve")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(this.thisType)
                .addAnnotation(Override.class)
                .addStatement("$T tx = new $T()", thisType, thisType)
                .addCode(setBlock.build())
                .addStatement("return tx")
                .build();

        classBuilder.addMethod(method);
    }

}
