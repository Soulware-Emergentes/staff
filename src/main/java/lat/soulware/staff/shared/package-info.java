/**
 * Base types every other module builds on: the aggregate and entity supertypes, the domain event
 * contract, the domain exception hierarchy, and the repository contract.
 *
 * <p>Declared open: modules may depend on the nested packages directly.
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN,
    displayName = "Shared Kernel"
)
package lat.soulware.staff.shared;
