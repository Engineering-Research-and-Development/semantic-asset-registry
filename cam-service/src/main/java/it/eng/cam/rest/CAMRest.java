package it.eng.cam.rest;

import it.eng.cam.finder.FinderFactory;
import it.eng.cam.rest.exception.CAMServiceWebException;
import it.eng.cam.rest.orion.context.ContextElement;
import it.eng.cam.rest.security.authentication.CAMPrincipal;
import it.eng.cam.rest.security.authorization.AssetOwnershipFilter;
import it.eng.cam.rest.security.authorization.DomainOwnershipFilter;
import it.eng.cam.rest.security.project.Project;
import it.eng.cam.rest.security.roles.Role;
import it.eng.cam.rest.security.service.AuthenticationService;
import it.eng.cam.rest.security.service.impl.IDMKeystoneService;
import it.eng.cam.rest.security.user.UserLoginJSON;
import it.eng.cam.rest.sesame.SesameRepoManager;
import it.eng.cam.rest.sesame.dto.AssetByModelJSON;
import it.eng.cam.rest.sesame.dto.AssetJSON;
import it.eng.cam.rest.sesame.dto.AttributeJSON;
import it.eng.cam.rest.sesame.dto.ClassJSON;
import it.eng.cam.rest.sesame.dto.RelationshipJSON;
import it.eng.cam.rest.sesame.utility.SesameRepoUtility;
import it.eng.ontorepo.Asset;
import it.eng.ontorepo.BeInCpps;
import it.eng.ontorepo.ClassItem;
import it.eng.ontorepo.OrionConfig;
import it.eng.ontorepo.PropertyDeclarationItem;
import it.eng.ontorepo.PropertyValueItem;
import it.eng.ontorepo.RepositoryDAO;
import it.eng.ontorepo.Util;
import it.eng.ontorepo.query.sparql.SparqlQueryFactory;
import it.eng.ontorepo.reasoner.pellet.PelletReasonerFactory;
import it.eng.ontorepo.spraqltemplate.GetModel;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.stream.Collectors;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@DeclareRoles({Role.BASIC, Role.ADMIN})  //Add new Roles if needed
@Path("/")
@PermitAll
public class CAMRest {
    public static final String OCB_ENABLED_READ_ONLY_MESSAGE = "This individual is linked to OCB and IS READ ONLY!";
    private static final Logger logger = LogManager.getLogger(CAMRest.class.getName());
    @Context
    SecurityContext securityContext;

    public CAMRest() {
    }
   
    
    /**
     * author ascatox the param flat = true gives a FLAT list of all classes
     *
     * @param flat
     * @return
     */
    @GET
    @Path("/classes")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClassItem> getClassHierarchy(@QueryParam("flat") boolean flat) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.getClasses(repoInstance, /*true*/false, flat);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/classes/{className}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClassItem> getIndividuals(@PathParam("className") String className) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<ClassItem> classes = CAMRestImpl.getClasses(repoInstance, false, false);
            ClassItem deepSearchClass = CAMRestImpl.deepSearchClasses(classes, className);
            if (deepSearchClass == null)
                return new ArrayList<ClassItem>();
            return deepSearchClass.getSubClasses();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/classes")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createClass(ClassJSON clazz) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.createClass(repoInstance, clazz.getName(), clazz.getParentName());
            return Response.ok("Class with name '" + clazz.getName() + "' was successfully created!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/classes/{className}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateClass(@PathParam("className") String className, ClassJSON clazz) {
        RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            if (!className.equalsIgnoreCase(clazz.getName())) {
                try {
                    CAMRestImpl.renameClass(repoInstance, className, clazz.getName());
                } catch (Exception e) {
                    logger.error(e);
                    throw new CAMServiceWebException(e.getMessage());
                } finally {
                    SesameRepoManager.releaseRepoDaoConn(repoInstance);
                }
                className = clazz.getName();
            }
            CAMRestImpl.moveClass(repoInstance, className, clazz.getParentName());
            return Response.ok("Class with name '" + className + "' has parent Class " + clazz.getParentName()).build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/classes/{className}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteClass(@PathParam("className") String className) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.deleteClass(repoInstance, className);
            return Response.ok("Class with name '" + className + "' was successfully deleted!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/classes/ancestors/{className}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getTreePath(@PathParam("className") String className) {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            if (null == className || "".equalsIgnoreCase(className.trim()))
                return new ArrayList<String>();
            return CAMRestImpl.getTreePath(repoInstance, className);
        } catch (Exception e) {
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    // FINE CLASSES
    // ASSETS

    //Modified in 2016-09-22 by ascatox
    //Models as template of assets doesn't exist anymore.
    @GET
    @Path("/assets/{assetName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Asset> getAssetByName(@PathParam("assetName") String assetName) {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            List<Asset> assets = AssetOwnershipFilter.filterAll(CAMRestImpl.getIndividuals(repoInstance), securityContext);
            if (null == assetName || "".equals(assetName.trim())) {
                return assets.stream()
                        .filter(asset ->
                                null != asset.getNamespace() && SesameRepoManager.getNamespace().equalsIgnoreCase(asset.getNamespace()))
                        .collect(Collectors.toList());
            }
            return assets.stream().filter(asset -> asset.getNormalizedName().equalsIgnoreCase(assetName))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/assets")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public List<Asset> getAssetsForClass(@QueryParam("className") String className,
                                         @QueryParam("retrieveForChildren") boolean retrieveForChildren) {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            if (null == className || "".equals(className.trim()))
                return getAssetByName(null);
            if (retrieveForChildren)
                return AssetOwnershipFilter.filterAll(CAMRestImpl.getIndividualsForChildren(repoInstance, className), securityContext);
            return AssetOwnershipFilter.filterAll(CAMRestImpl.getIndividuals(repoInstance, className), securityContext);
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/assets")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAsset(AssetJSON asset) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.createAsset(repoInstance, asset.getName(), asset.getModelName(), asset.getDomainName());
            return Response.ok("Asset with name '" + asset.getName() + "' for Model '" + asset.getModelName()
                    + "' for Domain '" + asset.getDomainName() + "' was successfully created!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/assets/{assetName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response updateAsset(@PathParam("assetName") String assetName, AssetJSON asset) {
        List<PropertyValueItem> individualAttributes = null;
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
           /* if (isOCBEnabled(assetName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, assetName);
            if (individualAttributes != null) {
                CAMRestImpl.editAsset(repoInstance, assetName, asset);
                return Response.ok("Asset with name '" + asset.getName() + "' for Model '" + asset.getModelName()
                        + "' for Domain '" + asset.getDomainName() + "' was successfully updated!").build();
            } else {
                return Response.notModified("Asset with name '" + asset.getName() + "' for Model '" + asset.getModelName()
                        + "' for Domain '" + asset.getDomainName() + "' does not exist").build();
            }
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/assets/{assetName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteAsset(@PathParam("assetName") String assetName) {
        RepositoryDAO repoInstance = null;
        try {
            if (isOCBEnabled(assetName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            cleanProps(assetName);
            CAMRestImpl.deleteIndividual(repoInstance, assetName);
            return Response.ok("Individual with name '" + assetName + "' was successfully deleted!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/assets/{assetName}/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public List<PropertyValueItem> getIndividualAttributes(@PathParam("assetName") String assetName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, assetName);
                        
            /* giaisg add for debug */
            for (PropertyValueItem pvi : individualAttributes) {
				System.err.println("normalizedValue=" + pvi.getNormalizedValue() + "\tnormalizedName=" + pvi.getNormalizedName()); 
			}
            /* end add */
            
            return individualAttributes.stream()
                    /* giaisg modify
                    .filter(item -> !item.getNormalizedValue().equals(OWL.OBJECTPROPERTY.stringValue())*/
            		.filter(item -> (!item.getNormalizedValue().equals(OWL.OBJECTPROPERTY.stringValue())
            				&& !item.getNormalizedValue().equals("ObjectProperty"))
            		/* end modify */
                            && !item.getNormalizedName().contains("system")
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/assets/{assetName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyValueItem getIndividualAttribute(@PathParam("assetName") String assetName,
                                                    @PathParam("attributeName") String attributeName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> attrs = CAMRestImpl.getIndividualAttributes(repoInstance, assetName);
            for (PropertyValueItem attr : attrs) {
                if (attr.getNormalizedName().equalsIgnoreCase(attributeName))
                    return attr;
            }
            return null;
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/assets/{assetName}/attributes/")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttribute(@PathParam("assetName") String assetName, AttributeJSON attribute) {
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
            /*if (isOCBEnabled(assetName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setAttribute(repoInstance, attribute.getName(), assetName, attribute.getValue(),
                    attribute.getType());
            return Response.ok("Attribute with name '" + attribute.getName() + "'for individual '" + attribute.getName()
                    + "' and value '" + attribute.getValue() + "' of type '" + attribute.getType()
                    + "' was successfully added!").build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/assets/{assetName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttribute(@PathParam("assetName") String assetName,
                                 @PathParam("attributeName") String attributeName, AttributeJSON attribute) {
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
            /*if (isOCBEnabled(assetName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, assetName, attributeName);
        } catch (Exception e) {
            logger.error(e);
        }
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setAttribute(repoInstance, attribute.getName(), assetName, attribute.getValue(),
                    attribute.getType());
            return Response.ok(
                    "Attribute with name '" + attribute.getName() + "'for individual '" + assetName + "' and value '"
                            + attribute.getValue() + "' of type '" + attribute.getType() + "' was successfully added!")
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/assets/{assetName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response removeAttribute(@PathParam("assetName") String assetName,
                                    @PathParam("attributeName") String attributeName) {
        RepositoryDAO repoInstance = null;
        try {
            if (isOCBEnabled(assetName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, assetName, attributeName);
            return Response.ok("Attribute with name '" + attributeName + "'for individual '" + assetName
                    + "' was successfully deleted!").build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/assets/{assetName}/relationships")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyValueItem> getRelationships(@PathParam("assetName") String assetName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, assetName);
            
            System.err.println(OWL.OBJECTPROPERTY.stringValue());            
            for (PropertyValueItem pvi : individualAttributes) {
				System.err.println("\tnormalizedValue=" + pvi.getNormalizedValue() + "\tnormalizedName=" + pvi.getNormalizedName()); 
			}
            
            return individualAttributes.stream()
                    .filter(item -> (item.getNormalizedValue().equals(OWL.OBJECTPROPERTY.stringValue())
                    		|| item.getNormalizedValue().equals("ObjectProperty"))
                            && !item.getNormalizedName().contains("system")
                            )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/assets/{assetName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyValueItem getRelationship(@PathParam("assetName") String assetName,
                                             @PathParam("relationshipName") String relationshipName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, assetName);
            for (PropertyValueItem propertyValueItem : individualAttributes) {
                if (propertyValueItem.getNormalizedName().equalsIgnoreCase(relationshipName)) {
                    return propertyValueItem;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/assets/{assetName}/relationships")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRelationship(@PathParam("assetName") String assetName, RelationshipJSON relationship) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setRelationship(repoInstance, relationship.getName(), assetName,
                    relationship.getReferredName());
            return Response.ok("Relation with name '" + relationship.getName() + "'between '" + assetName + "' and '"
                    + relationship.getReferredName() + "' was successfully created!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/assets/{assetName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRelationship(@PathParam("assetName") String assetName,
                                       @PathParam("relationshipName") String relationshipName, RelationshipJSON relationship) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, assetName, relationshipName);
        } catch (Exception e) {
            logger.error(e);
        }
        try {
            repoInstance = SesameRepoManager.getRepoInstance(null);
            CAMRestImpl.setRelationship(repoInstance, relationship.getName(), assetName,
                    relationship.getReferredName());
            return Response.ok("Relation with name '" + relationshipName + "'between '" + assetName + "' and '"
                    + relationship.getReferredName() + "' was successfully updated!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/assets/{assetName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteRelationship(@PathParam("assetName") String assetName,
                                       @PathParam("relationshipName") String relationshipName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, assetName, relationshipName);
            return Response.ok(
                    "Relation with name '" + relationshipName + "'with '" + assetName + "' was successfully deleted!")
                    .build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    // FINE ASSETS
    @GET
    @Path("/attributes")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyDeclarationItem> getAttributes() {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            return CAMRestImpl.getAttributes(repoInstance);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models/{modelName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Asset> getModelByName(@PathParam("modelName") String modelName) {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            List<Asset> models = AssetOwnershipFilter.filterAll(CAMRestImpl.getIndividuals(repoInstance), securityContext);
            if (null == modelName)
                return models.stream()
                        .filter(asset -> CAMRestImpl.isModel(repoInstance, getClass(), asset.getIndividualName()))
                        .collect(Collectors.toList());
            return models.stream()
                    .filter(model -> model.getNormalizedName().equalsIgnoreCase(modelName)
                            && CAMRestImpl.isModel(repoInstance, getClass(), model.getIndividualName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Asset> getModelsForClass(@QueryParam("className") String className) {
        final RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        try {
            if (null == className)
                return getModelByName(null);
            List<Asset> individuals = AssetOwnershipFilter.filterAll(CAMRestImpl.getIndividuals(repoInstance, className), securityContext);
            return individuals.stream()
                    .filter(indiv -> CAMRestImpl.isModel(repoInstance, getClass(), indiv.getIndividualName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/models")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAssetModel(AssetJSON model) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.createAssetModel(repoInstance, model.getName(), model.getClassName(), model.getDomainName());
            return Response.ok("Model with name '" + model.getName() + "' for Model '" + model.getClassName()
                    + "' for Domain '" + model.getDomainName() + "' was successfully created!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/models/{modelName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAssetModel(@PathParam("modelName") String modelName, AssetJSON model) {
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
        	/*
            if (isOCBEnabled(modelName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, modelName);
            if (individualAttributes != null) {
                CAMRestImpl.editAsset(repoInstance, modelName, model);
                return Response.ok("Model with name '" + model.getName() + "' for Model '" + model.getClassName()
                        + "' for Domain '" + model.getDomainName() + "' was successfully updated!").build();
            } else {
                return Response.notModified("Model with name '" + model.getName() + "' for Model '"
                        + model.getClassName() + "' for Domain '" + model.getDomainName() + "' does not exist").build();
            }
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/models/{modelName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteModel(@PathParam("modelName") String modelName) {
        RepositoryDAO repoInstance = null;
        try {
            if (isOCBEnabled(modelName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            cleanProps(modelName);
            CAMRestImpl.deleteIndividual(repoInstance, modelName);
            return Response.ok("Model with name '" + modelName + "' was successfully deleted!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models/{modelName}/attributes")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyValueItem> getModelAttributes(@PathParam("modelName") String modelName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, modelName);
            return individualAttributes.stream()
                    .filter(item -> item.getNormalizedName().contains("system")
                            || !item.getNormalizedValue().equals(OWL.OBJECTPROPERTY.stringValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models/{modelName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyValueItem getModelAttribute(@PathParam("modelName") String modelName,
                                               @PathParam("attributeName") String attributeName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> attrs = CAMRestImpl.getIndividualAttributes(repoInstance, modelName);
            for (PropertyValueItem attr : attrs) {
                if (attr.getNormalizedName().equalsIgnoreCase(attributeName))
                    return attr;
            }
            return null;
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/models/{modelName}/attributes/")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setModelAttribute(@PathParam("modelName") String modelName, AttributeJSON attribute) {
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
            /*if (isOCBEnabled(modelName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setAttribute(repoInstance, attribute.getName(), modelName, attribute.getValue(),
                    attribute.getType());
            return Response.ok("Attribute with name '" + attribute.getName() + "'for individual '" + attribute.getName()
                    + "' and value '" + attribute.getValue() + "' of type '" + attribute.getType()
                    + "' was successfully added!").build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/models/{modelName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setModelAttribute(@PathParam("modelName") String modelName,
                                      @PathParam("attributeName") String attributeName, AttributeJSON attribute) {
        RepositoryDAO repoInstance = null;
        try {
        	//Modified in 2018-08-03 by Mattia Marzano
        	//Allow to update asset when OCB is enabled
            /*if (isOCBEnabled(modelName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();*/
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, modelName, attributeName);
        } catch (Exception e) {
            logger.error(e);
        }
        try {
            CAMRestImpl.setAttribute(repoInstance, attribute.getName(), modelName, attribute.getValue(),
                    attribute.getType());
            return Response.ok(
                    "Attribute with name '" + attribute.getName() + "'for individual '" + modelName + "' and value '"
                            + attribute.getValue() + "' of type '" + attribute.getType() + "' was successfully added!")
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/models/{modelName}/attributes/{attributeName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeModelAttribute(@PathParam("modelName") String modelName,
                                         @PathParam("attributeName") String attributeName) {
        RepositoryDAO repoInstance = null;
        try {
            if (isOCBEnabled(modelName))
                return Response.status(405).entity(OCB_ENABLED_READ_ONLY_MESSAGE).build();
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, modelName, attributeName);
            return Response.ok("Attribute with name '" + attributeName + "'for individual '" + modelName
                    + "' was successfully deleted!").build();
        } catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models/{modelName}/relationships")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<PropertyValueItem> getModelRelationships(@PathParam("modelName") String modelName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, modelName);
            return individualAttributes.stream()
                    .filter(item -> !item.getNormalizedName().contains("system")
                            && item.getNormalizedValue().equals(OWL.OBJECTPROPERTY.stringValue()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/models/{modelName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public PropertyValueItem getModelRelationship(@PathParam("modelName") String modelName,
                                                  @PathParam("relationshipName") String relationshipName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, modelName);
            for (PropertyValueItem propertyValueItem : individualAttributes) {
                if (propertyValueItem.getNormalizedName().equalsIgnoreCase(relationshipName)) {
                    return propertyValueItem;
                }
            }
            return null;
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/models/{modelName}/relationships")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createModelRelationship(@PathParam("modelName") String modelName, RelationshipJSON relationship) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setRelationship(repoInstance, relationship.getName(), modelName,
                    relationship.getReferredName());
            return Response.ok("Relation with name '" + relationship.getName() + "'between '" + modelName + "' and '"
                    + relationship.getReferredName() + "' was successfully created!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/models/{modelName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateModelRelationship(@PathParam("modelName") String modelName,
                                            @PathParam("relationshipName") String relationshipName, RelationshipJSON relationship) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, modelName, relationshipName);
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.setRelationship(repoInstance, relationship.getName(), modelName,
                    relationship.getReferredName());
            return Response.ok("Relation with name '" + relationshipName + "'between '" + modelName + "' and '"
                    + relationship.getReferredName() + "' was successfully updated!").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/models/{modelName}/relationships/{relationshipName}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteModelRelationship(@PathParam("modelName") String modelName,
                                            @PathParam("relationshipName") String relationshipName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.removeProperty(repoInstance, modelName, relationshipName);
            return Response.ok(
                    "Relation with name '" + relationshipName + "'with '" + modelName + "' was successfully deleted!")
                    .build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/assets/{assetName}/orion/refresh")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response refreshAssetFromOCB(@PathParam("assetName") String assetName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            if (!isOCBEnabled(assetName))
                return Response.status(405).entity("Individual " + assetName + " is not linked to Orion").build();
            CAMRestImpl.refreshAssetFromOCB(repoInstance, assetName);
            return Response.ok(
                    "Data refreshing from OCB for Individual '" + assetName + "' was successful!")
                    .build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }


    // FINE MODELS

    // DOMAINS
    @GET
    @Path("/domains")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> getDomains() {
        try {
            IDMKeystoneService idmService = new IDMKeystoneService();
            return DomainOwnershipFilter.filterAll(idmService.getProjects(), securityContext);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        }
    }

    @GET
    @Path("/domains/{domainId}/assets")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Asset> getAssetsForDomain(@PathParam("domainId") String domainId) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<Asset> assets = AssetOwnershipFilter.filterAll(CAMRestImpl.getAssetsForDomain(repoInstance, domainId), securityContext);
            return assets.stream()
                    .filter(asset ->
                            asset.getNamespace().equalsIgnoreCase(SesameRepoManager.getNamespace()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authenticate(UserLoginJSON user) {
        try {
            if (Constants.AUTHENTICATION_SERVICE.equalsIgnoreCase(AuthenticationService.OAUTH2.name()))
                throw new UnsupportedOperationException("OAuth2 authentication is not allowed!");
            IDMKeystoneService service = new IDMKeystoneService();
            return service.authenticate(user);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        }
    }


    @GET
    @Path("/logged")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public CAMPrincipal getUserLogged(@Context HttpServletRequest httpRequest) {
        try {
            return (CAMPrincipal) securityContext.getUserPrincipal();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        }
    }

    @POST
    @Path("/orion/contexts/")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContextElement> createContexts(List<AssetJSON> assetJSONs) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.sendContexts(repoInstance, assetJSONs, true);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/orion/contexts")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<ContextElement> updateContexts(List<AssetJSON> assetJSONs) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.sendContexts(repoInstance, assetJSONs, false);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/orion/contexts/{name}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response disconnectFromContext(@PathParam("name") String assetName) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.disconnectAssetsFromOrion(repoInstance, assetName);
            return Response.ok("Asset disconnected successfully from Orion Context Broker").build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }


    @POST
    @Path("/orion/config")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrionConfig> createOrionConfigs(List<OrionConfig> orionConfigs) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.createOrionConfigs(repoInstance, orionConfigs);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @PUT
    @Path("/orion/config")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrionConfig> editOrionConfigs(List<OrionConfig> orionConfigs) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.editOrionConfigs(repoInstance, orionConfigs);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/orion/config")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<OrionConfig> getOrionConfigs() {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.getOrionConfigs(repoInstance);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @DELETE
    @Path("/orion/config/{configId}")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public Response deleteOrionConfig(@PathParam("configId") String orionConfigId) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.deleteOrionConfig(repoInstance, orionConfigId);
            return Response.ok(
                    "Orion Context Broker configuration with id '" + orionConfigId + "' was successfully deleted!")
                    .build();
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Path("/orion/{configId}/assets")
    @RolesAllowed({Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public List<Asset> getAssetsForOrionConfig(@PathParam("configId") String orionConfigId) {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            List<Asset> assets = AssetOwnershipFilter.filterAll(CAMRestImpl.getAssetsForOrionConfig(repoInstance, orionConfigId), securityContext);
            return assets.stream()
                    .filter(asset ->
                            asset.getNamespace().equalsIgnoreCase(SesameRepoManager.getNamespace()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @POST
    @Path("/idas/download")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces({"application/json"})
    public String downloadContexts(List<AssetJSON> assetJSONs) throws IOException {
        RepositoryDAO repoInstance = null;
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            return CAMRestImpl.createIDASMappingFile(repoInstance, assetJSONs);
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }

    @GET
    @Produces("text/html")
    public String summary(@Context HttpServletRequest httpRequest) {
        String content = "";
        StringBuilder contentBuilder = new StringBuilder();
        FinderFactory finder = FinderFactory.getInstance();
        try {
            URL url = getClass().getResource("/summary.html");
            File file = new File(url.toURI());
            BufferedReader in = new BufferedReader(new FileReader(file));
            String str;
            while ((str = in.readLine()) != null) {
                contentBuilder.append(str);
            }
            in.close();
            content = contentBuilder.toString();
            String serverUrl = httpRequest.getServerName();
            int serverPort = httpRequest.getServerPort();
            String protocol = "http";
            if (serverPort == 443)
                protocol = "https";
            content = content.replaceAll("camServiceUrl",
                    protocol + "://" + serverUrl + ":" + serverPort + "/CAMService");
            content = content.replaceAll("artifactInfo",
                    finder.getString("artifact.id") + " v" + finder.getString("version"));
        } catch (IOException e) {
            logger.error(e);
        } catch (URISyntaxException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
        return content;
    }
    
    @POST
    @Path("/SPARQLInferenceQuery")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response SPARQLInferenceQuery(String sparqlQuery) {
    	// check if all form parameters are provided
		if (sparqlQuery == null || sparqlQuery.isEmpty())
			return Response.status(400).entity("Invalid data input").build();

		String jsonResults = null;
        try {
        	jsonResults = PelletReasonerFactory.getInstance().executeInferenceQuery(sparqlQuery);
        } catch (Exception e) {
            logger.error("Unable to execute inference Query. Problem with Reasoner", e);
            throw new CAMServiceWebException(e.getMessage());
        } 
        return Response.ok(jsonResults, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("/SPARQLInferenceUpdate")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response SPARQLInferenceUpdate(String sparqlUpdate) {
    	// check if all form parameters are provided
		if (sparqlUpdate == null || sparqlUpdate.isEmpty())
			return Response.status(400).entity("Invalid data input").build();

		String jsonResults = null;
        try {
        	jsonResults = PelletReasonerFactory.getInstance().executeInferenceUdpate(sparqlUpdate);
        	if (jsonResults == null)
        		jsonResults = "ERROR: SPARQL Update Not Performed!";
        	
        } catch (Exception e) {
            logger.error("Unable to execute inference Update. Problem with Repository", e);
            throw new CAMServiceWebException(e.getMessage());
        } 
        return Response.ok(jsonResults, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("/SPARQLQuery")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response SPARQLQuery(String sparqlQuery) {
    	// check if all form parameters are provided
		if (sparqlQuery == null || sparqlQuery.isEmpty())
			return Response.status(400).entity("Invalid data input").build();
        String jsonResults = null;
        try {
        	jsonResults = SparqlQueryFactory.getInstance().sparqlQuery(sparqlQuery);
        } catch (Exception e) {
        	logger.error("Unable to execute SPARQL Query", e);
            throw new CAMServiceWebException(e.getMessage());
        } 
        return Response.ok(jsonResults, MediaType.APPLICATION_JSON).build();
    }
    
    @POST
    @Path("/SPARQLUpdate")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response SPARQLUpdate(String sparqlUpdate) {
    	// check if all form parameters are provided
		if (sparqlUpdate == null || sparqlUpdate.isEmpty())
			return Response.status(400).entity("Invalid data input").build();
        try {
        	SesameRepoUtility.getInstance().sparqlUpdate(sparqlUpdate);
        	return Response.ok("SPARQL Operation Performed", MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            logger.error("Unable to execute SPARQL Update", e);
            throw new CAMServiceWebException(e.getMessage());
        }
    }
    
    @POST
    @Path("/UploadRDFFile")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response UploadRDFFile(@FormDataParam("file") InputStream uploadedInputStream,
    		@FormDataParam("file") FormDataContentDisposition fileDetail) {
    	// check if all form parameters are provided
		if (uploadedInputStream == null || fileDetail == null)
			return Response.status(400).entity("Invalid data input").build();
		File rdfFile = null;
        try {
        	rdfFile = writeTempFile(uploadedInputStream, fileDetail.getFileName());
        	SesameRepoUtility.getInstance().addRdfFileToRepo(rdfFile, null, true);
        	return Response.ok("RDF File was successfully added to REPO!").build();
        } catch (Exception e) {
            logger.error("Unalbe to import RDF file", e);
            throw new CAMServiceWebException(e.getMessage());
        } finally {
        	if (rdfFile != null) {
        		rdfFile.delete();
        		rdfFile = null;
        		logger.info("RDF file successfully removed from the Server's TMP directory");
        	}
        }
    }
    
    // save uploaded file to new location
 	private File writeTempFile(InputStream uploadedInputStream, String fileName) {
 		File tempFile = null;
 		try {
 			//create a temp file and write it buffer content
 			tempFile = File.createTempFile(fileName, ".rdf");
			java.nio.file.Files.copy(uploadedInputStream, tempFile.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(uploadedInputStream);
 		} catch (IOException e) {
 			logger.error("Unable to write file", e);
 		}
 		return tempFile;
 	}

    private void cleanProps(String individualName) {
        RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance,
                individualName);
        for (PropertyValueItem propertyValueItem : individualAttributes) {
            try {
                repoInstance = SesameRepoManager.getRepoInstance(getClass());
                if (!propertyValueItem.getNormalizedName().contains(Util.PATH_TERM))
                    CAMRestImpl.removeProperty(repoInstance, individualName, propertyValueItem.getNormalizedName());
            } catch (Exception e) {
                logger.error(e);
                throw new RuntimeException(e);
            } finally {
                SesameRepoManager.releaseRepoDaoConn(repoInstance);
            }
        }
    }
    
    @POST
    @Path("/orion/assetbymodel")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAssetModelByModel( @QueryParam("link") boolean linkToOCB, AssetJSON model) {
        RepositoryDAO repoInstance = null;
        try {
        	
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.createAssetModel(repoInstance, model.getName(), model.getClassName(), model.getDomainName());       
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } 
        
        try {
        	
        	GetModel sparqlQuery = new GetModel();
        	String jsonResults = null;
            jsonResults = SparqlQueryFactory.getInstance().sparqlQuery(sparqlQuery.querystring("Event", model.getClassName()));
            
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResults);
            
            ArrayList<String> attname = new ArrayList<String>();
            for( JsonNode attributeloop : jsonNode.get("results").get("bindings")) {
            	String[] parts = attributeloop.get("o").get("value").textValue().split("#");
            	attname.add(parts[1]);    
            	
        	}
                   
            for (Iterator<String> iterator = attname.iterator(); iterator.hasNext();) {
				String attributename = iterator.next();
				CAMRestImpl.setAttribute(repoInstance, attributename, model.getName(), "", "java.lang.String");
			}
        	
        	attname = new ArrayList<String>();
        	if (linkToOCB) {
            	try {

            		ArrayList<AssetJSON> assetJSONs = new ArrayList<AssetJSON>();
            		assetJSONs.add(model);
            		CAMRestImpl.sendContexts(repoInstance, assetJSONs, true);
            		
                } catch (Exception e) {
                	 logger.error(e);
                     throw new CAMServiceWebException(e.getMessage());
                } 
            }
        	
        	return Response.ok("Asset was successfully created!").build();
            
         	   
       }   
        
        catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        }
                	
        finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }
    
  
    
    
    
    @PUT
    @Path("/orion/assetbymodel")
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAssetModelByModel(AssetByModelJSON model) {
    	
        RepositoryDAO repoInstance = null;
        try {
        	repoInstance = SesameRepoManager.getRepoInstance(getClass());
        	List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance, model.getAssetName());
            if (individualAttributes != null) {
            	AssetJSON editasset = new AssetJSON(model.getAssetName(), model.getClassName(), model.getModelName(), model.getDomainName(), model.getOrionConfigId());
                CAMRestImpl.editAsset(repoInstance, model.getAssetName(), editasset);
                }
                   
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } 
        
        try {
        	for( AttributeJSON attribute : model.getAttribute()) {
        		
        		repoInstance = SesameRepoManager.getRepoInstance(getClass());
                CAMRestImpl.removeProperty(repoInstance, model.getAssetName(), attribute.getName());
        		CAMRestImpl.setAttribute(repoInstance, attribute.getName(), model.getAssetName(), attribute.getValue(), attribute.getType());
        	}
        	if (isOCBEnabled(model.getAssetName())) {
            	try {
            		AssetJSON asset = new AssetJSON(model.getAssetName(), model.getClassName(), model.getModelName(), model.getDomainName(), model.getOrionConfigId());
            		ArrayList<AssetJSON> assetJSONs = new ArrayList<AssetJSON>();
            		assetJSONs.add(asset);
            		CAMRestImpl.sendContexts(repoInstance, assetJSONs, true);
            		
                } catch (Exception e) {
                	 logger.error(e);
                     throw new CAMServiceWebException(e.getMessage());
                } 
            }
        	
        	return Response.ok("Asset was successfully created!").build();
            
           	   
       }   
        
        catch (IllegalArgumentException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (RuntimeException e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
        }
                	
        finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }
    
    @POST
    @Path("/orion/class/assetbymodel")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Role.BASIC, Role.ADMIN})
    public ArrayList<AssetJSON> OCBLinkByClass(AssetJSON asset) {
    	RepositoryDAO repoInstance = null;
        
        
    	ArrayList<AssetJSON> assetJSONs = new ArrayList<AssetJSON>();    	    	
        try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            for( Asset assetlist : CAMRestImpl.getIndividuals(repoInstance, asset.getClassName())) {
            	
            	AssetJSON sent = new AssetJSON(assetlist.getIndividualName() , asset.getClassName(), asset.getModelName(), asset.getDomainName(), asset.getOrionConfigId());
            	assetJSONs.add(sent);
        		
        	};
        
            
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
            
        } try {
            repoInstance = SesameRepoManager.getRepoInstance(getClass());
            CAMRestImpl.sendContexts(repoInstance, assetJSONs, true);
        	return assetJSONs;
        	
         } catch (Exception e) {
        	e.printStackTrace();
            logger.error(e);
            throw new CAMServiceWebException(e.getMessage());
            
        }
        finally {
            SesameRepoManager.releaseRepoDaoConn(repoInstance);
        }
    }
    
     
    
    /**
     * If an asset is linked to the OCB is not possible to modify the asset is READ ONLY.
     * Every change should come from OCB.
     *
     * @param individualName
     * @return
     * @throws Exception
     */
    private Boolean isOCBEnabled(String individualName) throws Exception {
        if (StringUtils.isBlank(individualName)) {
            throw new Exception("Individual Name is empty!");
        }
        RepositoryDAO repoInstance = SesameRepoManager.getRepoInstance(getClass());
        List<PropertyValueItem> individualAttributes = CAMRestImpl.getIndividualAttributes(repoInstance,
                individualName);
        if (null == individualAttributes) {
            throw new Exception("Could not determine if OCB is enabled for individual: " + individualName);
        }
        SesameRepoManager.restartRepoDaoConn(repoInstance);
        long count = individualAttributes.stream().filter(indiv -> indiv.getNormalizedName().contains(BeInCpps.syncTo)).count();
        SesameRepoManager.restartRepoDaoConn(repoInstance);
        if (count > 0) return true;
        return false;
    }
       
}



